import { createPinia } from 'pinia'
import { createApp } from 'vue'

import App from './App.vue'
import router from './router'
import './assets/styles.css'

const savedTheme = localStorage.getItem('javachat.theme')
document.documentElement.dataset.theme = savedTheme === 'dark' ? 'dark' : 'light'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
