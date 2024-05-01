import {api, auth} from "../boot/axios";
import {OneRoleUser, StudentView, User, UserView} from "../model/User";
import {SubjectWithEvaluationDTO} from "../model/SubjectWithEvaluationDTO";
import {Subject, SubjectWithSchoolClassInformation} from "../model/Subject";
import axios, {AxiosResponse} from "axios";
import {SchoolClass} from "../model/SchoolClass";
import {School} from "../model/School";
import {SchoolUserRole} from "../model/SchoolUserRole";
import {SchoolPeriod, SchoolPeriodWithSchoolIds} from "../model/SchoolPeriod";
import {Request} from "../model/Request";
import {Evaluation} from "../model/Evaluation";
import {StudentWithEvaluationDTO} from "../model/StudentWithEvaluationDTO";
import {Calendar, Week} from "../model/Calendar";
import {PlannedSchoolLesson} from "../model/PlannedSchoolLesson";
import {SchoolLesson} from "../model/SchoolLesson";

export const login = async (email: string, password: string): Promise<AxiosResponse> =>
        await axios.post<string>(`/authenticate`, {username: email, password}, {
            baseURL: "/auth",
        })
export const logout = async () =>
        await axios.post(`/logout`, null, {
            baseURL: "/auth",
        })


export const loginAfterSelectedRole = async (roleId: number, periodId: number): Promise<AxiosResponse> =>
        await auth.post<string>(`/authenticate-after-selected-school`, null, {
            params: {roleId: roleId, periodId: periodId},
            baseURL: "/auth",
        })

export const getAllUsersBySchoolIdAndPeriodId = async (schoolId, periodId): Promise<UserView[]> =>
        await api.get<UserView[]>('/get-all-users-by-school-id-and-period-id', {
            params: {
                schoolId: schoolId,
                periodId: periodId
            }
        }).then(p => p.data)

export const getUserWithDetailsByUserId = async (userId, periodId, schoolId): Promise<OneRoleUser> =>
        await api.get<OneRoleUser>('/get-user-by-id', {
            params: {
                userId: userId,
                periodId: periodId,
                schoolId: schoolId
            }
        }).then(p => p.data)

export const getStudentsSubjectsAndEvaluations = async (studentId, periodId, schoolId, schoolClassId): Promise<SubjectWithEvaluationDTO[]> =>
        await api.post<SubjectWithEvaluationDTO[]>('/get-student-subjects-with-evaluation', null, {
            params: {
                studentId,
                periodId,
                schoolId,
                schoolClassId
            }
        }).then(p => p.data)
export const getEvaluationForSubjectAndSchoolClass = async (subjectId, periodId, schoolId, schoolClassId): Promise<StudentWithEvaluationDTO[]> =>
        await api.post<StudentWithEvaluationDTO[]>('/get-evaluation-for-subject-and-school-class', null, {
            params: {
                subjectId,
                periodId,
                schoolId,
                schoolClassId
            }
        }).then(p => p.data)


export const getAllSubjectsInSchoolClass = async (schoolClassId,
                                                  periodId, schoolId): Promise<Subject[]> =>
        await api.get<Subject[]>('/get-subjects-by-school-class', {
            params: {
                schoolClassId,
                schoolId,
                periodId
            }
        }).then(p => p.data)

export const fetchAllSubjectsTaughtByTeacher = async (teacherId, periodId, schoolId): Promise<SubjectWithSchoolClassInformation[]> =>
        await api.get<SubjectWithSchoolClassInformation[]>('/get-all-subjects-taught-by-teacher', {
            params: {
                teacherId, periodId, schoolId
            }
        }).then(p => p.data)

export const getAllSchoolClasses = async (): Promise<SchoolClass[]> =>
        await auth.get<SchoolClass[]>('/get-all-school-classes').then(p => p.data)

export const getAllSchools = async (): Promise<School[]> =>
        await auth.get<School[]>('/get-all-schools').then(p => p.data)

export const getAllUserRoles = async (userId: number): Promise<SchoolUserRole[]> =>
        await auth.get<SchoolUserRole[]>('/get-all-user-roles', {
            params: {userId: userId}
        }).then(p => p.data)

export const getAllSchoolPeriods = async (): Promise<SchoolPeriod[]> =>
        await auth.get<SchoolPeriod[]>('/get-all-periods').then(p => p.data)

export const getAllSchoolPeriodsWithTheSchoolsTheyAreStarted = async (): Promise<SchoolPeriodWithSchoolIds[]> =>
        await auth.get<SchoolPeriodWithSchoolIds[]>('/get-all-school-periods-with-school-ids').then(p => p.data)

export const getAllSubjects = async (): Promise<String[]> =>
        await auth.get<String[]>('/get-all-subjects').then(p => p.data)

export const findStudentByPhoneNumberPeriodAndSchoolClass = async (phoneNumber,
                                                                   periodId,
                                                                   schoolClassId): Promise<OneRoleUser> =>
        await auth.get<OneRoleUser>('/find-user-by-phone-number-period-class', {
            params: {
                phoneNumber,
                periodId,
                schoolClassId,
            }
        }).then(p => p.data)
export const findUserWithAllItsRolesByPhoneNumber = async (phoneNumber): Promise<User | null> =>
        await auth.get<User | null>('/find-user-with-all-its-roles-by-phone-number', {
            params: {
                phoneNumber
            }
        }).then(p => p.data)

export const createRequestFromUser = async (user): Promise<any> =>
        await auth.post<string>(`/create-requests`, user, {
            baseURL: "/auth",
        })


export const getUserRequestsBySchoolAndPeriod = async (periodId, schoolId, userId: number | null = null): Promise<Request[]> =>
        await api.get<Request[]>('/get-user-requests-by-school-and-period', {
            params: {
                periodId, schoolId, userId
            }
        }).then(p => p.data)

export const getRoleRequestsBySchoolAndPeriod = async (periodId, schoolId, userId: number | null = null): Promise<Request[]> =>
        await api.get<Request[]>('/get-role-requests-by-school-and-period', {
            params: {
                periodId, schoolId, userId
            }
        }).then(p => p.data)

export const changeRequestStatus = async (requestIds,
                                          requestStatus, resolvedByUserId): Promise<any> =>
        await api.post<string>(`/change-request-status`, Array.isArray(requestIds) ? requestIds : [requestIds], {
            params: {
                requestStatus,
                resolvedByUserId
            }
        })

export const fetchUserWithAllItsRolesById = async (id, schoolId, periodId): Promise<User> =>
        await api.get<User>('/get-user-with-all-roles', {
            params: {
                id, schoolId, periodId
            }
        }).then(p => p.data)

export const updateUser = async (user, loggedUserId): Promise<any> =>
        await api.post<any>(`/update-user`, user, {
            params: {loggedUserId},
            headers: {'Content-Type': 'application/json'}
        })

export const getSchoolClassesFromSchool = async (schoolId, periodId): Promise<SchoolClass[]> =>
        await api.get<SchoolClass[]>('/get-school-classes-from-school', {
            params: {
                schoolId, periodId
            }
        }).then(p => p.data)

export const getAllStudentsFromSchoolClass = async (schoolClassId, periodId): Promise<StudentView[]> =>
        await api.get<StudentView[]>('/get-all-students-from-school-class', {
            params: {
                schoolClassId, periodId
            }
        }).then(p => p.data)

export const getSchoolClassById = async (schoolClassId, periodId): Promise<SchoolClass> =>
        await api.get<SchoolClass>('/get-school-class-by-id', {
            params: {
                schoolClassId, periodId
            }
        }).then(p => p.data)

export const uploadUsersExcelFile = async (file, periodId,
                                           schoolId,
                                           schoolRole, schoolClassId: number | null = null): Promise<AxiosResponse<UserView[]>> => {
    const bodyFormData = new FormData()
    bodyFormData.append('file', file)
    return await api.post<UserView[]>(`/import-user-excel`, bodyFormData, {
        params: {
            periodId: periodId,
            schoolId: schoolId,
            schoolRole: schoolRole,
            schoolClassId: schoolClassId
        },
        headers: {"Content-Type": "multipart/form-data"},
    })
}

export const updateUserProfilePicture = async (profilePicture, userId) => {
    const bodyFormData = new FormData()
    bodyFormData.append('profilePicture', profilePicture)
    return await api.post(`/change-profile-picture`, bodyFormData, {
        params: {
            userId: userId,
        },
        headers: {"Content-Type": "multipart/form-data"},
    })
}

export const getUserProfilePicture = async (userId): Promise<File | null> =>
        await api.post<BlobPart>(`/get-user-profile-picture`, null, {
            params: {
                userId
            },
            responseType: 'blob'
        }).then(async response =>
                response.data?.size !== 0 ? new File([response.data], "profilePicture") : null
        )

export const createUser = async (user, loggedInUserId): Promise<User> => {
    const response: AxiosResponse<number> = await api.post<number>(`/create-user`, user, {
        params: {loggedInUserId: loggedInUserId},
        headers: {'Content-Type': 'application/json'}
    })

    return (<User>{
                ...user, id: response.data
            }
    )
}
export const createUserChangeStatusRequest = async (userId,
                                                    newStatus,
                                                    periodId,
                                                    schoolId,
                                                    loggedUserId): Promise<any> =>
        await api.post<any>(`/create-user-change-status-request`, null, {
            params: {
                userId: userId,
                newStatus: newStatus,
                periodId: periodId,
                schoolId: schoolId,
                loggedUserId: loggedUserId
            },
            headers: {'Content-Type': 'application/json'}
        })

export const createRoleChangeStatusRequest = async (roleId,
                                                    newStatus,
                                                    periodId,
                                                    schoolId,
                                                    loggedUserId): Promise<any> =>
        await api.post<any>(`/create-role-change-status-request`, null, {
            params: {
                roleId: roleId,
                newStatus: newStatus,
                periodId: periodId,
                schoolId: schoolId,
                loggedUserId: loggedUserId
            },
            headers: {'Content-Type': 'application/json'}
        })

export const resetPasswordRequest = async (email): Promise<AxiosResponse<boolean>> =>
        await auth.post<boolean>(`/reset-password-request`, null, {
            params: {
                email: email
            },
            headers: {'Content-Type': 'application/json'},
            baseURL: "/auth",
        })
export const updatePassword = async (newPassword,
                                     passwordResetToken): Promise<any> =>
        await auth.post<any>(`/update-password`, null, {
            params: {
                newPassword: newPassword,
                passwordResetToken: passwordResetToken
            },
            headers: {'Content-Type': 'application/json'},
            baseURL: "/auth",
        })

export const changeUserPasswordWithOldPasswordProvided = async (newPassword, oldPassword): Promise<any> =>
        await api.post<any>(`/change-user-password`, null, {
            params: {
                newPassword: newPassword,
                oldPassword: oldPassword
            },
            headers: {'Content-Type': 'application/json'}
        })


export const getAllTeachersThatDoNotHaveSchoolClass = async (schoolId, periodId): Promise<UserView[]> =>
        await api.get<UserView[]>('/get-all-teachers-that-do-not-have-school-class', {
            params: {
                schoolId, periodId
            }
        }).then(p => p.data)

export const fetchSubjectById = async (subjectId,
                                       periodId,
                                       schoolId): Promise<Subject> =>
        await api.get<Subject>('/get-subject-by-id', {
            params: {
                subjectId,
                periodId,
                schoolId
            }
        }).then(p => p.data)

export const saveSchoolClass = async (schoolClass, studentsFromClassFile: Blob | null = null): Promise<AxiosResponse<number>> => {
    const bodyFormData = new FormData()
    bodyFormData.append('schoolClass', JSON.stringify(schoolClass))
    if (studentsFromClassFile) {
        bodyFormData.append('studentsFromClassFile', studentsFromClassFile)
    }
    return await api.post<number>(`/save-school-class`, bodyFormData, {
        headers: {"Content-Type": "multipart/form-data"}
    })
}

export const syncNumbersInClass = async (schoolClassId, periodId): Promise<any> =>
        await api.post<any>(`/sync-numbers-in-class`, null, {
            params: {schoolClassId: schoolClassId, periodId: periodId},
            headers: {'Content-Type': 'application/json'}
        })

export const fetchAllStudentSubjectEvaluationsFromSchoolClass = async (schoolClass, evaluationType): Promise<AxiosResponse<Evaluation[]>> =>
        await api.post<Evaluation[]>(`/fetch-all-student-subject-evaluations-from-school-class`, schoolClass, {
            params: {evaluationType: evaluationType},
            headers: {'Content-Type': 'application/json'}
        })

export const saveEvaluations = async (evaluations, periodId, schoolId): Promise<AxiosResponse<StudentWithEvaluationDTO[]>> =>
        await api.post<StudentWithEvaluationDTO[]>(`/save-evaluations`, evaluations, {
            params: {periodId: periodId, schoolId: schoolId},
            headers: {'Content-Type': 'application/json'}
        })

export const updateEvaluations = async (evaluations, periodId, schoolId): Promise<AxiosResponse<StudentWithEvaluationDTO[]>> =>
        await api.post<StudentWithEvaluationDTO[]>(`/update-evaluations`, evaluations, {
            params: {periodId: periodId, schoolId: schoolId},
            headers: {'Content-Type': 'application/json'}
        })

export const updateSchoolLesson = async (schoolLesson: SchoolLesson): Promise<any> =>
        await api.post<any>(`/update-school-lesson`, schoolLesson, {
            headers: {'Content-Type': 'application/json'}
        })

export const fetchStudentById = async (studentId,
                                       schoolClassId,
                                       periodId): Promise<StudentView> =>
        await api.get<StudentView>('/get-student-by-id-school-and-period', {
            params: {
                studentId,
                schoolClassId,
                periodId
            }
        }).then(p => p.data)

export const fetchSchoolCalendarForSchoolAndPeriod = async (schoolId,
                                                            periodId): Promise<Calendar> =>
        await api.get<Calendar>('/fetch-school-calendar-for-school-and-period', {
            params: {
                schoolId: schoolId,
                periodId: periodId
            }
        }).then(p => p.data)


export const fetchPlannedSchoolLessonsForSchool = async (schoolId,
                                                         periodId): Promise<PlannedSchoolLesson[]> =>
        await api.get<PlannedSchoolLesson[]>('/get-planned-school-lessons-for-school', {
            params: {
                schoolId: schoolId,
                periodId: periodId
            }
        }).then(p => p.data)

export const fetchWeeksForSchoolClassSchoolAndPeriod = async (schoolClassName, schoolId,
                                                              periodId): Promise<Week[]> =>
        await api.get<Week[]>('/fetch-weeks-for-school-class-school-and-period', {
            params: {
                schoolClassName: schoolClassName,
                schoolId: schoolId,
                periodId: periodId
            }
        }).then(p => p.data)

export const fetchSchoolLessonsForSchoolClassWeekSchoolAndPeriod = async (schoolClassId,
                                                                          weekNumber,
                                                                          schoolId,
                                                                          periodId): Promise<SchoolLesson[]> =>
        await api.get<SchoolLesson[]>('/fetch-school-lessons-for-school-class-week-school-and-period', {
            params: {
                schoolClassId: schoolClassId,
                weekNumber: weekNumber,
                schoolId: schoolId,
                periodId: periodId,
            }
        }).then(p => p.data)

export const fetchSchoolLessonsForTeacherWeekSchoolAndPeriod = async (teacherId,
                                                                      weekNumber,
                                                                      schoolId,
                                                                      periodId): Promise<SchoolLesson[]> =>
        await api.get<SchoolLesson[]>('/fetch-school-lessons-for-teacher-week-school-and-period', {
            params: {
                teacherId: teacherId,
                weekNumber: weekNumber,
                schoolId: schoolId,
                periodId: periodId,
            }
        }).then(p => p.data)

export const fetchSchoolLessonById = async (schoolLessonId): Promise<SchoolLesson> =>
        await api.get<SchoolLesson>('fetch-school-lesson-by-id', {
            params: {
                schoolLessonId: schoolLessonId
            }
        }).then(p => p.data)
