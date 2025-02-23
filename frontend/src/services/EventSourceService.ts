export let actionsEventSource;

const isFunction = (functionToCheck) => functionToCheck && {}.toString.call(functionToCheck) === '[object Function]'

const debounce = (func, wait) => {
    let timeout;
    let waitFunc;

    return function () {
        if (isFunction(wait)) {
            waitFunc = wait;
        } else {
            waitFunc = () => wait;
        }

        // @ts-ignore
        let context = this, args = arguments;
        let later = () => {
            timeout = null;
            func.apply(context, args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, waitFunc());
    };
}

// reconnectFrequencySeconds doubles every retry
let reconnectFrequencySeconds = 2;

const reconnectFunc = debounce(() => {
    setupActionsEventSource();
    // Double every attempt to avoid overwhelming server
    reconnectFrequencySeconds *= 2;
    // Max out at ~1 minute as a compromise between user experience and server load
    if (reconnectFrequencySeconds >= 64) {
        reconnectFrequencySeconds = 64;
    }
}, () => reconnectFrequencySeconds * 1000);

export const setupActionsEventSource = () => {
    console.log("Subscribing to actions")
    actionsEventSource = new EventSource("/api/stream/subscribe-actions")
    actionsEventSource.onopen = (e) => {
        console.log("Connection opened")
        // Reset reconnect frequency upon successful connection
        reconnectFrequencySeconds = 2;
    };
    actionsEventSource.onerror = (e) => {
        console.log("Stream Error: ", e)
        actionsEventSource.close();
        reconnectFunc();
    };
}