(ns cms.routes.article
  (:require [cms.layout :as layout]
            [compojure.core :refer [defroutes GET POST context]]
            [ring.util.http-response :refer [ok]]
            [cms.db :refer [wcar*]]
            [taoensso.carmine :as car]
            [clj-yaml.core :as yaml]
            [ring.util.response :refer [redirect]]))

(defn render [template & [params]]
  (layout/render template (assoc params :current "namespaces")))

(defn detail-page [nsid id]
  (let [namespace (wcar* (car/get (str "namespaces:" nsid)))
        [item schema] (wcar* (car/get (str "articles:" nsid ":" id))
                             (car/get (str "templates:" (:template namespace))))
        schema (yaml/parse-string schema)
        fields (map #(assoc % :value (get item (keyword (:name %)))) schema)]
    (render "articles-detail.html" {:item item
                                    :nsid nsid
                                    :id id
                                    :fields fields})))

(defn create-page [nsid]
  (let [ns (wcar* (car/get (str "namespaces:" nsid)))
        schema (wcar* (car/get (str "templates:" (:template ns))))]
    (render "articles-create.html" {:schema (yaml/parse-string schema)})))

(defn create [nsid params]
  (let [{:keys [id]} params]
    (wcar* (car/set (str "articles:" nsid ":" id) params)
           (car/lpush (str "namespaces:" nsid ":articles") id))
    (redirect (str "/namespaces/" nsid "/articles/" id))))

(defn delete [nsid id]
  (wcar* (car/del (str "articles:" nsid ":" id))
         (car/lrem (str "namespaces:" nsid ":articles") 1 id))
  (redirect (str "/namespaces/" nsid)))

(defn list [session]
  (let [{{id :id} :identity} session
        namespaces (wcar* (car/lrange (str "users:" id ":namespaces") 0 -1))
        counts (map #(wcar* (car/llen (str "namespaces:" % ":articles"))) namespaces)]
    (render "articles-list.html"
            {:namespaces (map (fn [id count] {:id id :count count}) namespaces counts)})))

(defn update [nsid id params session]
  (wcar* (car/set (str "articles:" nsid ":" id) params))
  (redirect (str "/namespaces/" nsid "/articles/" id)))

(defroutes article-routes
  (context "/articles" []
           (GET "/" {session :session} (list session)))
  (context "/namespaces/:nsid/articles" [nsid]
           (GET "/new" [] (create-page nsid))
           (POST "/new" {:keys [params]} (create nsid params))
           (POST "/" {:keys [params]} (create nsid params))
           (context "/:id" [id]
                    (GET "/" [] (detail-page nsid id))
                    (POST "/" {:keys [session params]} (update nsid id params session))
                    (POST "/delete" [] (delete nsid id)))))
