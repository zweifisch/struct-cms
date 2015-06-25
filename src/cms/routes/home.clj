(ns cms.routes.home
  (:require [cms.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :refer [ok]]
            [cms.db :refer [wcar* register]]
            [taoensso.carmine :as car]
            [buddy.hashers :refer [encrypt check]]
            [ring.util.response :refer [redirect]]))

(def installed (future (wcar* (car/get "installed"))))

(defn dashboard-page [session]
  (let [{:keys [role]} (:identity session)
        [templates users namespaces] (wcar* (car/llen "templates")
                                            (car/llen "users")
                                            (car/llen "namespaces"))]
    (layout/render "home.html" {:role role
                                :templates templates
                                :users users
                                :current "dashboard"
                                :namespaces namespaces})))

(defn home-page [session]
  (let [{{role :role} :identity} session]
    (if (= :admin role)
      (dashboard-page session)
      (redirect "/articles"))))

(defn install-page []
  (when-not @installed
    (layout/render "install.html")))

(defn init [{:keys [login password]}]
  (when-not @installed
    (register login password :admin)
    (wcar* (car/set "installed" true))
    (def installed (future (wcar* (car/get "installed"))))
    (redirect "/")))

(defn login-page []
  (layout/render "login.html"))

(defn check-auth [username password]
  (let [user (wcar* (car/get (str "users:" username)))]
    (when (check password (:password user)) (assoc user :id username))))

(defn login [{:keys [params session]}]
  (let [{:keys [username password]} params 
        identity (check-auth username password)]
    (if identity
      (-> (redirect "/")
          (assoc :session (assoc session :identity identity)))
      (redirect "/login"))))

(defn logout [session]
  (-> (redirect "/login")
      (assoc :session (assoc session :identity nil))))

(defroutes home-routes
  (GET "/" {:keys [session]} (home-page session))
  (GET "/install" [] (install-page))
  (POST "/install" {:keys [params]} (init params))
  (GET "/login" [] (login-page))
  (POST "/login" req (login req))
  (GET "/logout" {:keys [session]} (logout session)))
