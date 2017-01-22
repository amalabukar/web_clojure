(ns web-clojure.core
  (:require [compojure.core :as c]
            [ring.adapter.jetty :as j]
            [hiccup.core :as h]
            [ring.middleware.params :as p]
            [ring.util.response :as r])
  (:gen-class))

(defonce server (atom nil))
(defonce items (atom []))

(c/defroutes app
  (c/GET "/" []
    (h/html [:html
             [:body
              [:form {:action "/add-item" :method "post"}   
               [:input {:type "text" :placeholder "Enter Grocey item" :name "item"}]
               [:input {:type "text" :placeholder "how many" :name "status"}]
               [:button {:type "submit"} "Add item"]]
              [:ol
               (map (fn [item]
                      [:li item])
                 @items)]]]))
  (c/POST "/add-item" request
    (let [params (get request :params)
          item (get params "item")
          status(get params "status")
          list(list item " " status)]
      (swap! items conj list)
      (spit "items.edn"(pr-str @items))
      (r/redirect "/"))))

(defn -main []
  (try
    (let [items-str (slurp "items.edn")
          items-vac(read-string items-str)]
      (reset! items items-vac))
    (catch Exception _))
  (when @server
    (.stop @server))
  (let[app (p/wrap-params app)]
    (reset! server (j/run-jetty app {:port 3000 :join? false}))))
  
