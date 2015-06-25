(ns cms.routes.user
  (:require [cms.layout :as layout]
            [compojure.core :refer [defroutes GET POST context]]
            [ring.util.http-response :refer [ok]]
            [cms.db :refer [wcar* register]]
            [taoensso.carmine :as car]
            [buddy.hashers :refer [encrypt check]]
            [ring.util.response :refer [redirect]]))

(defn render [template & [params]]
  (layout/render template (assoc params :current "users")))

(defn create [{:keys [login password]}]
  (register login password :editor)
  (redirect "/users"))

(defn list-page []
  (let [items (wcar* (car/lrange "users" 0 -1))]
    (render "users-list.html" {:items items})))

(defn create-page []
  (render "users-create.html"))

(defn detail-page [id]
  (let [[item namespaces all-namespaces] (wcar* (car/get (str "users:" id))
                                                (car/lrange (str "users:" id ":namespaces") 0 -1)
                                                (car/lrange "namespaces" 0 -1))]
    (render "users-detail.html" {:item item
                                 :id id
                                 :all-namespaces all-namespaces
                                 :namespaces namespaces})))

(defn delete [id]
  (wcar* (car/del (str "users:" id))
         (car/lrem "users" 1 id))
  (redirect (str "/users")))

(defn namespace-delete [id namespace]
  (wcar* (car/lrem (str "users:" id ":namespaces") 1 namespace))
  (redirect (str "/users/" id)))

(defn namespace-assign [id namespace]
  (wcar* (car/lpush (str "users:" id ":namespaces") namespace))
  (redirect (str "/users/" id)))

(defroutes user-routes
  (context "/users" []
           (GET "/" [] (list-page))
           (GET "/new" [] (create-page))
           (POST "/" {:keys [params]} (create params))
           (context "/:id" [id]
                    (GET "/" [id] (detail-page id))
                    (POST "/namespaces/:namespace/delete" [namespace] (namespace-delete id namespace))
                    (POST "/namespaces" [namespace] (namespace-assign id namespace))
                    (POST "/delete" [id] (delete id)))))
