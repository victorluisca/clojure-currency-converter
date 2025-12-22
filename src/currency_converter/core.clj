(ns currency-converter.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clj-http.client :as http]))

(defn get-json
  [url params]
  (try
    (let [response (http/get url {:query-params params :as :json})]
      (:body response))
    (catch Exception e
      {:error :http-error
       :message (.getMessage e)})))

(defn fetch-rates
  [amount from to]
  (get-json "https://api.frankfurter.dev/v1/latest"
            {:amount amount
             :from   from
             :to     to}))

(def cli-options
  [["-f" "--from CURRENCY" "Source currency code"]
   ["-t" "--to CURRENCY"   "Target currency code"]
   ["-l" "--list"          "List all supported currencies"]
   ["-h" "--help"]])

(defn -main
  [& args]
  (parse-opts args cli-options))
