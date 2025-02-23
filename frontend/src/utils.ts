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

export const translationOfEvaluationValue = {
    GRADE: "оценка",
    FEEDBACK: "отзив",
    ABSENCE: "отсъствие",
}
export const translationOfSemester = {
    FIRST: "Първи срок",
    SECOND: "Втори срок",
    YEARLY: "Годишна",
}
export const translationOfWorkingDays = {
    MONDAY: "Понеделник",
    TUESDAY: "Вторник",
    WEDNESDAY: "Сряда",
    THURSDAY: "Четвъртък",
    FRIDAY: "Петък"
}
export const getRangeOf = (start, stop, step) => Array.from({length: ((stop - start) / step) + 1}, (_, i) => start + (i * step))


export const confirmActionPromiseDialog = (title: string, message: string | null = null) => new Promise<void>((resolve) => {
    Dialog.create({
        title: title,
        message: message ? message : '',
        ok: {
            label: "OK",
            color: 'primary'
        },
        cancel: {
            label: "Отказ",
            color: 'primary'
        },
        persistent: true
    }).onOk(() => resolve())
            .onCancel(() => {
            })
});
export const getRequestStatusColorClass = (requestStatus: RequestStatus | undefined) => {
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

    return date.formatDate(ev, "YYYY-MM-DD");
}


export const formatToBulgarian = (dateToFormat: string | null) => {
    if (dateToFormat == null || dateToFormat == '') {
        return null
    }
    return date.formatDate(new Date(dateToFormat), 'DD.MM.YYYY');
};

export const dateTimeToGermanLocaleString = (date) => {
    if (!date) return ''

    let dateToConvert: Date = date

    if (typeof date === 'string') {
        dateToConvert = new Date(date)
    }

    return dateToConvert.toLocaleDateString('de-DE', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    })
}

export const dateTimeToBulgarianLocaleString = (date) => {
    if (!date) return ''

    let dateToConvert: Date = date

    if (typeof date === 'string') {
        dateToConvert = new Date(date)
    }

    return dateToConvert.toLocaleDateString('en-US', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    }).slice(0, 20)
}
export const formatWithDash = (dateToFormat: string | null) => {
    if (dateToFormat == null || dateToFormat == '') {
        return null
    }
    return date.formatDate(new Date(dateToFormat), 'YYYY/MM/DD');
};
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

export const formatTime = (dateString: string): string => {
    let dateObject: Date = new Date(dateString);
    let hours: string = dateObject.getHours().toString().padStart(2, '0');
    let minutes: string = dateObject.getMinutes().toString().padStart(2, '0');
    return `${hours}:${minutes}`;
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

export const commentPromiseDialog = (): Promise<string> => new Promise<string>((resolve) => {
    Dialog.create({
        title: 'Добави коментар',
        prompt: {
            model: '',
            isValid: val => val.length > 2,
            type: 'text'
        },
        cancel: {
            label: 'Отказ',
            color: 'negative'
        },
        ok: {
            color: 'primary'
        },
        persistent: true,
    })
            .onOk((comment) => resolve(comment))
            .onCancel(() => {
            })
});
export const checkIfMaxDate = (date: Date | string | null | undefined, dates: Date[]): boolean => {
    if (!date) return false; // Handle null/undefined cases

    const inputDate = new Date(date); // Ensure `date` is a Date object
    if (isNaN(inputDate.getTime())) return false; // Check for invalid date

    const test: number[] = dates
            .map(it => it ? new Date(it).getTime() : NaN)
            .filter(timestamp =>
                    !isNaN(timestamp) && new Date(timestamp).toDateString() === inputDate.toDateString()
            );

    return test.length > 0 && inputDate.getTime() === Math.max(...test);
};

export function getDistinctDates(dates: (Date | string)[]): Date[] {
    const uniqueDates = new Set(
            dates
                    .map(date => {
                        const validDate = date instanceof Date ? date : new Date(date);
                        return isNaN(validDate.getTime()) ? null : validDate.toISOString().split('T')[0];
                    })
                    .filter(dateStr => dateStr !== null) // Remove invalid dates
    );

    return Array.from(uniqueDates).map(dateStr => new Date(dateStr!));
}