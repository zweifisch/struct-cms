(ns cms.routes.namespace
  (:require [cms.layout :as layout]
            [compojure.core :refer [defroutes GET POST context]]
            [ring.util.http-response :refer [ok]]
            [cms.db :refer [wcar*]]
            [taoensso.carmine :as car]
            [ring.util.response :refer [redirect]]))

(defn render [template & [params]]
  (layout/render template (assoc params :current "namespaces")))

(defn list-page []
  (let [namespaces (wcar* (car/lrange "namespaces" 0 -1))
        counts (map #(wcar* (car/llen (str "namespaces:" % ":articles"))) namespaces)]
    (render "namespaces-list.html"
            {:namespaces (map (fn [id count] {:id id :count count}) namespaces counts)})))

(defn detail-page [name]
  (let [[namespace articles] (wcar* (car/get (str "namespaces:" name))
                                    (car/lrange (str "namespaces:" name ":articles") 0 -1))]
    (render "namespaces-detail.html" {:namespace namespace
                                      :articles articles
                                      :id name})))

(defn create-page []
  (let [templates (wcar* (car/lrange "templates" 0 -1))]
    (render "namespaces-create.html" {:templates templates})))

(defn create [params]
  (let [{:keys [name]} params]
    (wcar* (car/lpush "namespaces" name)
           (car/set (str "namespaces:" name) params)))
  (redirect "/namespaces"))

(defn delete [id]
  (wcar* (car/del (str "namespaces:" id))
         (car/lrem "namespaces" 1 id))
  (redirect (str "/namespaces")))

(defroutes namespace-routes
  (context "/namespaces" []
           (GET "/" [] (list-page))
           (GET "/new" [] (create-page))
           (GET "/:id" [id] (detail-page id))
           (POST "/:id/delete" [id] (delete id))
           (POST "/" {:keys [params]} (create params))))
