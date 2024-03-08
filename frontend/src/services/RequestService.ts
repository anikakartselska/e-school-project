import {api, auth} from "../boot/axios";
import {OneRoleUser, StudentView, User, UserView} from "../model/User";
import {SubjectWithEvaluationDTO} from "../model/SubjectWithEvaluationDTO";
import {Subject, SubjectWithSchoolClassInformation} from "../model/Subject";
import axios, {AxiosResponse} from "axios";
import {SchoolClass} from "../model/SchoolClass";
import {School} from "../model/School";
import {SchoolUserRole} from "../model/SchoolUserRole";
import {SchoolPeriod, SchoolPeriodWithSchoolIds} from "../model/SchoolPeriod";

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

export const getAllUsersBySchoolId = async (schoolId): Promise<UserView[]> =>
        await api.get<UserView[]>('/get-all-users-by-school-id', {params: {schoolId: schoolId}}).then(p => p.data)

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

