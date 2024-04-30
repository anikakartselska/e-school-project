import {date, Dialog, Notify} from "quasar";
import {RequestStatus} from "./model/RequestStatus";

export const translationOfRoles = {
    ADMIN: "АДМИН",
    TEACHER: "УЧИТЕЛ",
    STUDENT: "УЧЕНИК",
    PARENT: "РОДИТЕЛ"
}

export const translationOfGender = {
    MALE: "МЪЖ",
    FEMALE: "ЖЕНА"
}

export const translationOfRequestStatus = {
    APPROVED: "Одобрена",
    PENDING: "Нерешена",
    REJECTED: "Отхвърлена",
}
export const translationOfRequestStatusForUser = {
    APPROVED: "АКТИВЕН",
    PENDING: "ИЗЧАКВАЩ",
    REJECTED: "НЕАКТИВЕН",
}
export const translationOfRequestStatusForRole = {
    APPROVED: "АКТИВНА",
    PENDING: "ИЗЧАКВАЩА",
    REJECTED: "НЕАКТИВНА",
}
export const getRangeOf = (start, stop, step) => Array.from({length: ((stop - start) / step) + 1}, (_, i) => start + (i * step))


export const confirmActionPromiseDialog = (title: string, message: string) => new Promise<void>((resolve) => {
    Dialog.create({
        title: title,
        message: message,
        ok: {
            label: "OK",
            color: 'primary'
        },
        persistent: true
    }).onOk(() => resolve())
            .onCancel(() => {
            })
});
export const getRequestStatusColorClass = (requestStatus: RequestStatus) => {
    switch (requestStatus) {
        case RequestStatus.APPROVED:
            return `text-green`;
        case RequestStatus.PENDING:
            return 'text-blue';
        case RequestStatus.REJECTED:
            return 'text-red';
    }
}
export const formatToDate = (ev) => {
    if (ev == null || ev == '') {
        return null
    }
    debugger
    return date.formatDate(ev, "YYYY-MM-DD");
}


export const formatToBulgarian = (dateToFormat: string | null) => {
    if (dateToFormat == null || dateToFormat == '') {
        return null
    }
    return date.formatDate(new Date(dateToFormat), 'DD.MM.YYYY');
};
export const formatWithDash = (dateToFormat: string | null) => {
    if (dateToFormat == null || dateToFormat == '') {
        return null
    }
    return date.formatDate(new Date(dateToFormat), 'YYYY/MM/DD');
};
export const isNumberArray = (arr: any[]) => {
    return Array.isArray(arr) && arr.every(element => typeof element === 'number');
}
export const getDatesInRange = (startDateStr: string, endDateStr: string): Date[] => {
    // Parse the date strings into Date objects
    const startDate = new Date(startDateStr.replace(/\//g, '-')); // Replace '/' with '-' for cross-browser compatibility
    const endDate = new Date(endDateStr.replace(/\//g, '-'));

    const datesInRange: Date[] = [];
    let currentDate = new Date(startDate);

    // Iterate through the dates, adding each one to the array
    while (currentDate <= endDate) {
        datesInRange.push(new Date(currentDate));
        // Move to the next day
        currentDate.setDate(currentDate.getDate() + 1);
    }

    return datesInRange;
}

export const isDateInRange = (targetDate: string, fromDate: string, toDate: string): boolean => {
    // Parse the strings into Date objects
    const target = new Date(targetDate.replace(/-/g, '/')); // Replace '-' with '/' for cross-browser compatibility
    const from = new Date(fromDate.replace(/-/g, '/'));
    const to = new Date(toDate.replace(/-/g, '/'));

    // Check if the target date falls within the range
    return target >= from && target <= to;
}

export const notifyForError = (title: string, caption: string, setTimeout: boolean = false) =>
        Notify.create({
            message: title,
            caption: caption,
            color: 'negative',
            icon: "report_problem",
            progress: true,
            timeout: setTimeout ? 5000 : 0,
            actions: [
                {label: 'Dismiss', color: 'yellow'}
            ],
            multiLine: true,
        })
export const confirmActionPromiseDialogWithCancelButton = (title: string, message: string) => new Promise<void>((resolve) => {
    Dialog.create({
        title: title,
        message: message,
        ok: {
            label: "OK",
            color: 'primary'
        },
        cancel: {
            label: "Отказ",
            color: 'negative'
        },
        persistent: true
    }).onOk(() => resolve())
            .onCancel(() => {
            })
});