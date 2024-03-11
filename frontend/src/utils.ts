import {Dialog} from "quasar";

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