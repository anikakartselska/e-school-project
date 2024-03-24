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

export const formatToBulgarian = (dateToFormat: string | null) => {
    if (dateToFormat == null || dateToFormat == '') {
        return null
    }
    return date.formatDate(new Date(dateToFormat), 'DD.MM.YYYY');
};
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