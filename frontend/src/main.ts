import {createApp} from 'vue'
import 'material-icons/iconfont/material-icons.css';
import "quasar/dist/quasar.prod.css";
import App from './App.vue'
import {Dialog, Notify, Quasar} from "quasar";
import {router} from "./router";

createApp(App)
        .use(Quasar, {
            plugins: {
                Dialog,
                Notify
            },
            config: {
                loadingBar: {
                    color: 'purple',
                    size: '200px',
                    position: 'bottom'
                }
            }
        })
        .use(router)
        .mount("#app");
