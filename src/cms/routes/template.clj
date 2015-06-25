(ns cms.routes.template
  (:require [cms.layout :as layout]
            [compojure.core :refer [defroutes context GET POST]]
            [ring.util.http-response :refer [ok]]
            [cms.db :refer [wcar*]]
            [taoensso.carmine :as car]
            [ring.util.response :refer [redirect]]))

(defn render [template & [params]]
  (layout/render template (assoc params :current "templates")))

(defn list-page []
  (let [templates (wcar* (car/lrange "templates" 0 -1))]
    (render "template-list.html" {:templates templates})))

(defn detail-page [name]
  (let [template (wcar* (car/get (str "templates:" name)))]
    (render "template-detail.html" {:template template})))

(defn create-page []
  (render "template-create.html"))

(defn create [params]
  (let [{:keys [schema name]} params]
    (prn params)
    (wcar* (car/set (str "templates:" name) schema)
           (car/lpush "templates" name))
    (redirect "/templates")))

(defn update [id params]
  (let [{:keys [schema]} params]
    (wcar* (car/set (str "templates:" id) schema))
    (redirect (str "/templates/" id))))

(defn delete [id]
  (wcar* (car/del (str "templates:" id))
         (car/lrem "templates" 1 id))
  (redirect (str "/templates")))

(defroutes template-routes
  (context "/templates" []
           (GET "/" [] (list-page))
           (GET "/new" [] (create-page))
           (POST "/new" {:keys [params]} (create params))
           (context "/:id" [id]
                    (GET "/" [] (detail-page id))
                    (POST "/" {params :params} (update id params))
                    (POST "/delete" [] (delete id)))))
