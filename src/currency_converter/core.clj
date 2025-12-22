(ns currency-converter.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clj-http.client   :as http]
            [clojure.string    :as str]))

(defn get-json
  ([url]
   (get-json url {}))
  ([url params]
   (try
     (let [opts (cond-> {:as :json}
                  (seq params) (assoc :query-params params))
           response (http/get url opts)]
       (:body response))
     (catch Exception e
       {:error   :http-error
        :message (.getMessage e)}))))

(defn fetch-rates
  [amount from to]
  (get-json "https://api.frankfurter.dev/v1/latest"
            {:amount amount
             :from   from
             :to     to}))

(defn fetch-currencies
  []
  (get-json "https://api.frankfurter.dev/v1/currencies"))

(def cli-options
  [["-f" "--from CURRENCY" "Source currency code"
    :default "USD"
    :parse-fn #(str/upper-case %)]
   ["-t" "--to CURRENCY"   "Target currency code"
    :default "BRL"
    :parse-fn #(str/upper-case %)]
   ["-l" "--list"          "List all supported currencies"]
   ["-h" "--help"]])

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (println options arguments errors summary)))
