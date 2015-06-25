(ns cms.routes.api
  (:require [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :refer [ok bad-request!]]
            [cms.db :refer [wcar*]]
            [taoensso.carmine :as car]
            [aussen.core :refer [env]]
            [clojure.pprint :refer [pprint]]
            [ring.util.response :refer [redirect]]))

(defn articles-list [ns & [{:keys [limit shift] :or {limit "10" shift "0"}}]]
  (let [limit (Integer/parseInt limit)
        shift (Integer/parseInt shift)
        ids (wcar* (car/lrange (str "namespaces:" ns ":articles") shift (dec (+ limit shift))))]
    (when (not-empty ids)
      (ok (wcar* (apply car/mget (map #(str "articles:" ns ":" %) ids)))))))

(defn articles-get [ns id]
  (let [article (wcar* (car/get (str "articles:" ns ":" id)))]
    (when (not-empty article)
      (ok article))))

(defn namespaces-list []
  (ok (wcar* (car/lrange "namespaces" 0 -1))))

(defroutes api-routes
  (GET "/api" [] (namespaces-list))
  (GET "/env" [] (prn env))
  (GET "/api/:ns/:id" [ns id] (articles-get ns id))
  (GET "/api/:ns" [ns :as {params :params}] (articles-list ns params)))
