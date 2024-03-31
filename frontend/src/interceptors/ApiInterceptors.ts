import {AxiosError, AxiosInstance, AxiosRequestConfig, AxiosResponse} from "axios";
import {QVueGlobals} from 'quasar'
import {router} from "../router";
import {notifyForError} from "../utils";
import {Queue} from "../model/Queue";
import {SmsError} from "../model/SmsError";

// Interceptors for '/api' URL prefix
const notificationQueue: Map<string, Queue<Function>> = new Map<string, Queue<Function>>()

export const setupApiInterceptorsTo = (axiosInstances: AxiosInstance[], quasar: QVueGlobals) => {
    axiosInstances.map(instance => {
        instance.interceptors.request.use(onRequest(quasar), onRequestError(quasar));
        instance.interceptors.response.use(onResponse(), onResponseError(quasar));
    });
};

const excludedUrlsFromNotifications = ['/stream/get-actions-with-filters-and-pagination', '/user/update-current-user-preferences']

const onRequest = (quasar: QVueGlobals) => (config: AxiosRequestConfig): AxiosRequestConfig => {
    if (config.method != 'get' && config.url && !excludedUrlsFromNotifications.includes(config.url)) {
        if (!notificationQueue.get(config.url)) {
            notificationQueue.set(config.url, new Queue<Function>())
        }
        notificationQueue.get(config.url)?.push(quasar.notify({
            progress: true,
            type: 'ongoing',
            position: 'top-right',
            message: 'progressMessage',
        }))
    }
    return config;
};

const onResponse = () => (response: AxiosResponse): AxiosResponse => {
    if (response.config.url && notificationQueue.get(response.config.url) && notificationQueue.get(response.config.url)?.isNotEmpty()) {
        // @ts-ignore
        notificationQueue.get(response.config.url).pop()(successNotification(response))
    }
    return response;
};

const onRequestError = (quasar: QVueGlobals) => (error: AxiosError): Promise<AxiosError> => {
    return onErrorAction(error, quasar);
};

const onResponseError = (quasar: QVueGlobals) => (error: AxiosError): Promise<AxiosError> => {
    return onErrorAction(error, quasar)
};

const onErrorAction = async (error: AxiosError, quasar: QVueGlobals) => {
    debugger
    if (error.config.url && notificationQueue.get(error.config.url) && notificationQueue.get(error.config.url)?.isNotEmpty()) {
        // @ts-ignore
        notificationQueue.get(error.config.url).pop()(failedNotification(error))
    }
    if (error.response?.status == 401) {
        notifyForError("Invalid Credentials", "Your credentials have expired. Please log in again!")
        router.push("/login");
    } else if (error.response?.status == 403) {
        notifyForError("Access Denied!", "You don't have the permission to do that!")
    } else if (error.response?.status == 404) {
        router.push("/404");
    } else if (error.response?.status == 418) { // 418 code is rvm business error status which is handled in each component.
        const businessError: SmsError = error.response.config.responseType === 'blob' ? JSON.parse(await (<Blob>error.response.data).text()) : error.response.data
        handleBusinessError(businessError, quasar)
    } else {
        const data = error.response?.data
        let messageHtml;
        if (
                typeof data === 'object' &&
                !Array.isArray(data) &&
                data !== null && !(data instanceof Blob)
        ) {
            messageHtml = constructMessageHtml(data?.exception, data?.message, data?.trace, data.timestamp, data?.path,)
        } else if (data instanceof Blob) {
            const parsedBlob = JSON.parse(await data.text())
            messageHtml = constructMessageHtml(parsedBlob?.exception, parsedBlob?.message, parsedBlob?.trace, parsedBlob.timestamp, parsedBlob?.path,)
        } else {
            messageHtml = JSON.stringify(error.response)
        }
        quasar.dialog({
            html: true,
            title: ` ${error.response?.statusText ?? 'error'} (${error.response?.status})`,
            message: messageHtml,
            persistent: true,
            position: "right",
            ok: {
                push: true,
                color: 'negative'
            },
        })
    }
    return Promise.reject(error);
};

const successNotification = (response: AxiosResponse) => ({
    type: 'positive',
    progress: true,
    position: 'top-right',
    timeout: 1000,
    message: 'successMessage'
});

const failedNotification = (error: AxiosError) => ({
    type: 'negative',
    progress: true,
    position: 'top-right',
    timeout: 1000,
    message: 'errorMessage'
});

const handleBusinessError = (businessError: SmsError, quasar: QVueGlobals) => {

    quasar.dialog({
        title: businessError.errorType,
        message: businessError.message,
        position: "right",
        ok: true,
    })
}

const constructMessageHtml = (exceptionTitle: string, message: string, trace: string, timestamp: Date, path: string): string =>
        `<b><u>Message:</u></b><br>${message && message != '' ? message : "Something went wrong! See stack trace for more information"}<br>
         <b><u>Path:</u></b><br>${path}<br>
         <b><u>Timestamp:</u></b><br>${timestamp}<br><br>
         <b><u>Exception:</u></b>: ${exceptionTitle}<br>
         <details>
            <summary><b><u>Click here to see stack Trace:</u></b></summary>
            <p>${trace}</p>
         </details>`
