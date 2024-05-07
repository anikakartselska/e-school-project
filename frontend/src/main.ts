import {createApp} from 'vue'
import 'material-icons/iconfont/material-icons.css';
import "quasar/dist/quasar.prod.css";
import App from './App.vue'
import {Dialog, Notify, Quasar} from "quasar";
import {router} from "./router";
import VueQrcode from 'qrcode';

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
        .component(VueQrcode.name, VueQrcode)
        .use(router)
        .mount("#app");
