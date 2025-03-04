export interface GradingScale {

    interval2: Interval,

    interval3: Interval,

    interval4: Interval,

    interval5: Interval,

    interval6: Interval,
}

export interface Interval {
    startingPoints: string,
    endingPoints: string
}