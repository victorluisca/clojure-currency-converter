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

(defn display-conversion
  [data target-currency]
  (if (:error data)
    (str "Error: " (:message data))
    (let [amount (:amount data)
          base (:base data)
          rate (get-in data [:rates (keyword target-currency)])]
      (str amount " " base " = " rate " " target-currency))))

(defn display-currencies
  [currencies]
  (if (:error currencies)
    (str "Error: " (:message currencies))
    (->> currencies
         (sort)
         (map (fn [[code full-name]] (str "  " (name code) "  " full-name)))
         (str/join "\n"))))

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
    (cond
      (:help options)
      (println summary)

      errors
      (println (str/join "\n" errors))

      (:list options)
      (println (display-currencies (fetch-currencies)))

      :else
      (if-let [amount (some-> (first arguments) parse-double)]
        (let [target (:to options)
              result (fetch-rates amount (:from options) target)]
          (println (display-conversion result target)))
        (println "Error: please provide a valid numeric amount.")))))
