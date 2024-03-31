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

export const getStudentsSubjectsAndEvaluations = async (schoolClassId, studentId, periodId, schoolId): Promise<SubjectWithEvaluationDTO[]> =>
        await api.get<SubjectWithEvaluationDTO[]>('/get-student-subjects-with-evaluation', {
            params: {
                schoolClassId,
                studentId,
                periodId,
                schoolId
            }
        }).then(p => p.data)

export const getAllStudentsFromClass = async (schoolClassId, periodId): Promise<StudentView[]> =>
        await api.get<StudentView[]>('/get-all-students-from-class', {
            params: {
                schoolClassId,
                periodId
            }
        }).then(p => p.data)

export const getSubjectsEvaluationsBySchoolClass = async (subjectId, schoolClassId,
                                                          periodId, schoolId): Promise<SubjectWithEvaluationDTO[]> =>
        await api.get<SubjectWithEvaluationDTO[]>('/get-all-evaluations-from-subject-in-school-class', {
            params: {
                subjectId,
                schoolClassId,
                schoolId,
                periodId
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
                                           schoolRole,
                                           userId, schoolClassId: number | null = null) => {
    const bodyFormData = new FormData()
    bodyFormData.append('file', file)
    return await api.post(`/import-user-excel`, bodyFormData, {
        params: {
            periodId: periodId,
            schoolId: schoolId,
            schoolRole: schoolRole,
            schoolClassId: schoolClassId,
            userId: userId,
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
        await api.post<any>(`/change-user-change-status-request`, null, {
            params: {
                userId: userId,
                newStatus: newStatus,
                periodId: periodId,
                schoolId: schoolId,
                loggedUserId: loggedUserId
            },
            headers: {'Content-Type': 'application/json'}
        })