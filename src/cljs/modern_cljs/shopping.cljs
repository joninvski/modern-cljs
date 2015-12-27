(ns modern-cljs.shopping
  (:require [domina.core :refer [add-class!
                                 append! 
                                 by-class
                                 by-id 
                                 destroy!
                                 remove-class!
                                 set-value!
                                 set-text!
                                 text
                                 value]]
            [domina.events :refer [listen! prevent-default]]
            [domina.css :refer [sel]]
            [hiccups.runtime]
            [modern-cljs.shopping.validators :refer [validate-shopping]]
            [shoreleave.remotes.http-rpc :refer [remote-callback]])
  (:require-macros [hiccups.core :refer [html]]
                   [shoreleave.remotes.macros :as macros]))

(defn validate-shopping-field [field text]
  (let [attr (name field)
        label (sel (str "label[for=" attr "]"))]
    (remove-class! label "help")
    (if-let [error (validate-shopping field (value (by-id attr)))]
      (do
        (add-class! label "help")
        (set-text! label error)
        false)
      (do
        (set-text! label text)
        true))))

(defn calculate [evt]
  (let [quantity (value (by-id "quantity"))
        price (value (by-id "price"))
        tax (value (by-id "tax"))
        discount (value (by-id "discount"))]
    (remote-callback :calculate
                     [quantity price tax discount]
                     #(set-value! (by-id "total") (.toFixed % 2)))
    (prevent-default evt)))

(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    ;; close on original labels' texts
    (let [quantity-text (text (sel "label[for=quantity]"))
          price-text (text (sel "label[for=price]"))
          tax-text (text (sel "label[for=tax]"))
          discount-text (text (sel "label[for=discount]"))]
      ;; blur quantity
      (listen! (by-id "quantity")
               :blur
               (fn [_] (validate-shopping-field :quantity quantity-text)))
      ;; blur price
      (listen! (by-id "price")
               :blur
               (fn [_] (validate-shopping-field :price price-text)))
      ;; blur tax
      (listen! (by-id "tax")
               :blur
               (fn [_] (validate-shopping-field :tax tax-text)))
      ;; blur discount
      (listen! (by-id "discount")
               :blur
               (fn [_] (validate-shopping-field :discount discount-text))))
    
    ;; click
    (listen! (by-id "calc") 
             :click 
             (fn [evt] (calculate evt)))
    ;; mouseover button
    (listen! (by-id "calc") 
             :mouseover 
             (fn []
               (append! (by-id "shoppingForm")
                        (html [:div.help "Click to calculate"]))))  ;; hiccups
    ;; mouseout button
    (listen! (by-id "calc") 
             :mouseout 
             (fn []
               (destroy! (by-class "help"))))))

