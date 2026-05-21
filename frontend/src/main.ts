import { createPinia } from 'pinia'
import { createApp } from 'vue'

import App from './App.vue'
import router from './router'
import './assets/styles.css'

const savedTheme = localStorage.getItem('javachat.theme')
document.documentElement.classList.toggle('dark', savedTheme === 'dark')

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
