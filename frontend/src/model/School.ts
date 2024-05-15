export interface School {
    id: number,
    schoolName: string,
    city: string,
    address: string
    rooms: RoomToSubjects[] | null
}

export interface RoomToSubjects {
    room: string
    subjects: string[]
}

export const roomToSubjectsText = (roomToSubjects: RoomToSubjects) => {
    return `${roomToSubjects.room} ${roomToSubjects.subjects.length > 0 ? ` - Стая за часове по: ${roomToSubjects.subjects.join(',')}` : ''}`
}