(ns dynamics-clj.core-scratch
  (:require [dynamics-clj.core :as api]
            [base64-clj.core :as base64]
            [clj-http.client :as client]
            [clojure.java.io :as io]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [proto-repl.saved-values]))

(def user "username here")
(def password "Some base64 encoded password")
(def config {:crmwebapipath "https://recruitercrm2.regent.edu/CRMRECRUIT/api/data/v8.0/"
             :ntlm [user (base64/decode password) nil nil]})

(defonce contacts (api/get-query-by-name config "contacts" "PhoneCall Audit" true))

(defn map-kv
  "Maps `f` on the vals of `m`, producing a new map with the same keys."
  [f m]
  (reduce-kv #(assoc %1 %2 (f %3)) {} m))

(defn try-dateparse-map [col]
  (map-kv (fn [val]
              (if (and (string? val) (re-find  #"\d{4}(-\d{2}){2}" val))
                  (f/parse (subs val 0 10))
                  val))
          col))

(defn get-ids [c]
  (let [apps (get c "datatel_contact_application")
        opps (get c "opportunity_parent_contact")
        appids (map #(get % "datatel_applicationid") apps)
        oppids (map #(get % "opportunityid") opps)]
       (assoc c "ids" (vec (concat appids oppids)))))

(def query "?$select=createdon,regent_firstcontactdate,fullname&$expand=datatel_contact_application($select=datatel_applicationid),opportunity_parent_contact($select=opportunityid)")

(defonce dispositions (api/get-query-by-name config "gap_powerbioptionsetrefs" "Dispositions" true))
(defn find-disp [value] (let [disp (first (filter #(= (get % "gap_value") value)
                                                  dispositions))]
                          (if disp disp (throw (Exception. (str "No Disposition found with value " value))))))

(defn get-phonecalls [{:strs [contactid ids fullname] c-createdon "createdon" :as c}]
  (let [filter (str "regent_disposition ne null and (_regardingobjectid_value eq " contactid
                    (reduce (fn [prev curr]
                              (str prev " or _regardingobjectid_value eq " curr))
                            "" ids)
                    ")")
        phcalls (api/retrieve-multiple config
                                      "phonecalls"
                                      ["createdon" "regent_disposition"] filter)
        parsed (map try-dateparse-map phcalls)
        earliest-createdon (t/earliest (into [c-createdon] (map #(get % "createdon") parsed)))
        mapped-phcalls (map (fn [{:strs [regent_disposition] ph-createdon "createdon" :as phcall}]
                             (let [disp (-> (find-disp regent_disposition)
                                            (get "ops_data")
                                            (subs 5))
                                   days (t/in-days (t/interval earliest-createdon ph-createdon))
                                   invalid (= "i" (subs disp 1 2))
                                   contacted (= "c" (subs disp 2 3))]
                               {"fullname" fullname
                                "invalid" invalid
                                "contacted" contacted
                                "day" days
                                "createdon" ph-createdon
                                "contactid" contactid
                                 ;; Disp code kept for checking my work as I go
                                "disp-code" disp}))


                            parsed)]
       (vec mapped-phcalls)))

(defn map-over-ph [ph-col] (map get-phonecalls ph-col))

(defn get-base [x] (-> (api/retrieve* config (str "contacts(" (get x "contactid") ")" query))
                       (:body)
                       (get-ids)
                       (try-dateparse-map)))


(def base (map get-base contacts))
(defonce phcalls (flatten (map-over-ph base)))

(def earliest-contacts (reduce (fn [prev {:strs [contactid invalid contacted createdon]}]
                                 (if (or invalid contacted)
                                  (let [earliest (if (contains? prev contactid)
                                                   (t/earliest (get prev contactid)
                                                     createdon)
                                                   createdon)]
                                    (assoc prev contactid earliest))
                                  prev))
                           {} phcalls))

(defn add-fallout [{:strs [createdon contactid] :as ph}]
       (let [earliest (get earliest-contacts contactid)
             fallout (or (t/equal? createdon earliest)
                         (t/after? createdon earliest))]
            (assoc ph "fallout" fallout)))

(defn format-vals [m]
  (map-kv (fn [val]
              (cond (= (type val) org.joda.time.DateTime)
                    (f/unparse (f/formatters :date) val)
                    (= true val) 1
                    (= false val) 0
                    :else val))
          m))

(defonce final (map (fn [x] (-> x add-fallout format-vals)) phcalls))

(defn write-csv
  "Takes a file (path, name and extension) and
   csv-data (vector of vectors with all values) and
   writes csv file."
  [file csv-data]
  (with-open [writer (io/writer file)]
    (csv/write-csv writer csv-data)))

(defn maps->csv-data
  "Takes a collection of maps and returns csv-data
   (vector of vectors with all values)."
  [maps]
  (let [columns (-> maps first keys)
        headers (mapv name columns)
        rows (mapv #(mapv % columns) maps)]
    (into [headers] rows)))

(defn write-csv-from-maps
  "Writes a collection of maps to a file in csv format"
  [file maps]
  (->> maps maps->csv-data (write-csv file)))

(write-csv-from-maps "./data.csv" final)
