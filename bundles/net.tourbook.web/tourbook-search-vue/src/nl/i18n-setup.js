import Vue from 'vue'
import VueI18n from 'vue-i18n'
import messages from './messages'

Vue.use(VueI18n)

const i18n = new VueI18n({
   //
   locale: getBrowserLocale(),
   fallbackLocale: 'en',
   messages
})

export default i18n

function getBrowserLocale() {
   //
   const browserLanguage = navigator.language || navigator.languages[0]

   let userLanguage = browserLanguage.substring(0, 2)

   console.log(userLanguage)

   return userLanguage
}
