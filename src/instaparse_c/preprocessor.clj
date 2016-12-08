(ns instaparse-c.preprocessor 
  (:refer-clojure :exclude [cat comment string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

;;;TODO \n is significant in preprocessor
(def preprocessor
  {
   :mcc/raw
   (cat 
    (star (altnt :mcc.raw/macro :mcc.raw/not-macro))
    (hs? "\n")) ;;no idea why this needs to be here sometimes

   :mcc.raw/not-macro
   (alt
    (cat 
     (neg (alts "/*" "*/"))
     (regexp #"[^# \t\n\r]+")
     )
    )

   :mcc.raw/macro
   (altnt :mcc.raw/define :mcc.raw/if :mcc.raw/ifdef :mcc.raw/include)
   
   :mcc.raw/define 
   (cat (hs "#" "define")
        (altnt :c11.macro.define/value :c11.macro.define/function) )

   :mcc.raw.define/value
   (cat (nt :c11/symbol) (nt :c11/expression))

   :mcc.raw.define/function 
   (cat (nt :c11/symbol) (parens (list-of (nt :c11/symbol)))
        (brackets? (star (nt :c11/statement))))

   :mcc.raw/if (cat
                  (hs "#" "if")
                  (nt :c11/expression)
                  (nt :mcc/raw)
                  (cat? (hs "#" "else")
                        (nt :mcc/raw))
                  (hs "#" "endif"))

   ;;TODO: ifndef
   :mcc.raw/ifdef (cat
                     (hs "#" "ifdef")
                     (nt :c11/expression)
                     (nt :mcc/raw)
                     (cat? (hs "#" "else")
                           (nt :mcc/raw))
                     (hs "#" "endif"))

   :mcc.raw/include
   (altnt :mcc.raw.include/header :mcc.raw.include/source)
   :mcc.raw.include/header
   (cat (hs "#" "include" "<") (regexp "[a-z0-9/]+\\.h") (hs ">"))
   :mcc.raw.include/source
   (cat (hs "#" "include" "\"") (regexp "[a-z0-9/]+\\.h") (hs "\""))

   :mcc.raw/value
   (regexp "[0-9]+")})

