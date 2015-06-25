(ns cms.db
  (:require [taoensso.carmine :as car :refer (wcar)]
            [buddy.hashers :refer [encrypt check]]
            [aussen.core :refer [env]]))

(defmacro wcar* [& body] `(car/wcar {:spec (:redis env)} ~@body))

(defn register [login password role]
  (let [encrypted (encrypt password {:algorithm :bcrypt+sha512})]
    (wcar* (car/set (str "users:" login) {:role role :password encrypted})
           (car/lpush "users" login))))
