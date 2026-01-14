(ns currency-converter.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clj-http.client   :as http]
            [clojure.string    :as str])
  (:gen-class))

(defn get-json
  "Makes an HTTP GET request to the given url.
   Optionally takes a map of query parameters.
   Returns the JSON-parsed body as a map or an error map if the request fails."
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
  "Requests the latest exchange rates from the Frankfurter API
   for a specific amount and currency pair."
  [amount from to]
  (get-json "https://api.frankfurter.dev/v1/latest"
            {:amount amount
             :from   from
             :to     to}))

(defn fetch-currencies
  "Fetches a map of all currency codes and their full names
   from the Frankfurter API."
  []
  (get-json "https://api.frankfurter.dev/v1/currencies"))

(defn format-usage
  "Returns usage string.
   Optionally takes a summary to display together with the usage."
  ([]
   (->> ["Usage:"
         "  currency-converter <amount> [OPTIONS]"
         ""
         "For more information, try \"--help\"."]
        (str/join "\n")))
  ([summary]
   (->> ["Currency Converter CLI"
         ""
         "Usage:"
         "  currency-converter <amount> [OPTIONS]"
         ""
         "Options:"
         summary]
        (str/join "\n"))))

(defn display-conversion
  "Formats the conversion result into a readable string.
   If the input contains an error, returns a formatted error message
   with usage instructions."
  [data target-currency]
  (if (:error data)
    (str "Error: " (:message data) "\n\n" (format-usage))
    (let [amount (:amount data)
          base (:base data)
          rate (get-in data [:rates (keyword target-currency)])]
      (str amount " " base " = " rate " " target-currency))))

(defn display-currencies
  "Formats the map of available currencies into a sorted list string.
   If the inputs contains an error, returns a formatted error message
   with usage instructions."
  [currencies]
  (if (:error currencies)
    (str "Error: " (:message currencies) "\n\n" (format-usage))
    (let [header "Supported currencies:\n"
          list-str (->> currencies
                        (sort)
                        (map (fn [[code full-name]] (str "    " (name code) "  " full-name)))
                        (str/join "\n"))]
      (str header list-str))))

(def cli-options
  [["-f" "--from CURRENCY" "Source currency code"
    :parse-fn #(str/upper-case %)]
   ["-t" "--to CURRENCY"   "Target currency code"
    :parse-fn #(str/upper-case %)]
   ["-l" "--list"          "List all supported currencies"]
   ["-h" "--help"]])

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options)
      (println (format-usage summary))

      errors
      (do (doseq [e errors]
            (println (str "Error: " (str/lower-case e))))
          (println "\n" (format-usage)))

      (:list options)
      (println (display-currencies (fetch-currencies)))

      :else
      (if-let [amount (some-> (first arguments) parse-double)]
        (let [target (:to options)
              result (fetch-rates amount (:from options) target)]
          (println (display-conversion result target)))
        (do (println "Error: please provide a valid numeric amount.")
            (println "\n" (format-usage)))))))
