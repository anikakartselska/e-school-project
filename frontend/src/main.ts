import {createApp} from 'vue'
import 'material-icons/iconfont/material-icons.css';
import "quasar/dist/quasar.prod.css";
import App from './App.vue'
import {Dark, Dialog, Notify, Quasar} from "quasar";
import {router} from "./router";
import VueQrcode from 'qrcode';

createApp(App)
        .use(Quasar, {
            plugins: {
                Dialog,
                Notify,
                Dark
            },
            config: {
                loadingBar: {
                    color: 'purple',
                    size: '200px',
                    position: 'bottom'
                }
            },
            dark: false,
        })
        .component(VueQrcode.name, VueQrcode)
        .use(router)
        .mount("#app");
