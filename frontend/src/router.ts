import MainPage from './pages/MainPage.vue'
import Home from './pages/Home.vue'
import Component from './pages/Component.vue'
import {createRouter, createWebHistory} from "vue-router";
import UsersPage from "./pages/user/users-page.vue";
import UserPage from "./pages/user/user-page.vue";
import GradesTabPerStudent from "./pages/per-student-evaluations/grades-tab.vue";
import AbsenceTabPerStudent from "./pages/per-student-evaluations/absence-tab.vue";
import FeedbackTabPerStudent from "./pages/per-student-evaluations/feedback-tab.vue";
import StudentSubjectsAndEvaluation from "./pages/per-student-evaluations/student-subjects-and-evaluation.vue";
import LoginSingIn from "./pages/login-signin.vue";
import LoginTab from "./pages/login-tab.vue";
import SignInTab from "./pages/sign-in-tab.vue";
import {userIsLoggedIn} from "./services/LocalStorageService";
import RequestsPage from "./pages/requests/requests-page.vue";
import {periodId, schoolId} from "./model/constants";
import UserRequestsTab from "./pages/requests/user-requests-tab.vue";
import RoleRequestsTab from "./pages/requests/role-requests-tab.vue";
import UsersTab from "./pages/user/users-tab.vue";
import ImportUsersTab from "./pages/user/import-users-tab.vue";
import SchoolClassesPage from "./pages/school-class/school-classes-page.vue";
import SchoolClassPage from "./pages/school-class/school-class-page.vue";
import ResetPasswordPage from "./pages/reset-password/reset-password-page.vue";
import NewPasswordInputPage from "./pages/reset-password/new-password-input-page.vue";
import SchoolClassStudentsTab from "./pages/school-class/school-class-students-tab.vue";
import SchoolClassSubjectsTab from "./pages/school-class/school-class-subjects-tab.vue";
import SchoolClassGradesTab from "./pages/school-class/school-class-grades-tab.vue";
import SchoolClassRemarksTab from "./pages/school-class/school-class-feedbacks-tab.vue";
import SchoolClassAbsencesTab from "./pages/school-class/school-class-absences-tab.vue";
import SubjectEvaluationsPerSchoolClass from "./pages/per-subject-evaluations/subject-evaluations-per-school-class.vue";
import SubjectGradesTab from "./pages/per-subject-evaluations/subject-grades-tab.vue";
import SubjectAbsenceTab from "./pages/per-subject-evaluations/subject-absence-tab.vue";
import SubjectFeedbackTab from "./pages/per-subject-evaluations/subject-feedback-tab.vue";
import CalendarPage from "./pages/administration/calendar-page.vue";
import ProgramPage from "./pages/administration/program-page.vue";
import SchoolClassLessonsTab from "./pages/school-class/school-class-lessons-tab.vue";
import SchoolLessonPage from "./pages/school-lesson/school-lesson-page.vue";
import SchoolLessonInformation from "./pages/school-lesson/school-lesson-information.vue";
import SchoolClassPlanTab from "./pages/school-class/school-class-plan-tab.vue";
import SchoolPage from "./pages/administration/school-page.vue";
import SchoolClassesPlansPage from "./pages/administration/school-classes-plans-page.vue";
import SchoolClassesPlanPage from "./pages/administration/school-classes-plan-page.vue";
import TeacherLessonsPage from "./pages/teacher-pages/teacher-lessons-page.vue";

const routes = [
    {
        path: '/login-sign-in', component: LoginSingIn, props: false,
        children: [
            {path: '/login', component: LoginTab, name: "login"},
            {path: '/sign-in', component: SignInTab, name: "sign-in"},
        ],
    },
    {
        path: '/reset-password', component: ResetPasswordPage
    },
    {
        path: '/new-password/:token', component: NewPasswordInputPage, props: true,
    },
    {
        path: '/',
        component: MainPage,
        redirect: to => ({path: `/home/${periodId.value}/${schoolId.value}`}),
        beforeEnter: (to, from, next) => {
            if (!userIsLoggedIn()) return next('/login')
            return next()
        },
        children: [
            {path: '/home/:periodId(\\d+)/:schoolId(\\d+)', component: Home, props: true},
            {path: '/component/:year(\\d+)/:month(\\d+)', component: Component, props: true},
            {
                path: '/users/:periodId(\\d+)/:schoolId(\\d+)',
                component: UsersPage,
                name: 'users',
                props: true,
                children: [
                    {
                        path: 'all',
                        component: UsersTab
                    },
                    {
                        path: 'import-users',
                        component: ImportUsersTab
                    },
                ],
            },
            {
                path: '/user/:id(\\d+)/:periodId(\\d+)/:schoolId(\\d+)',
                name: 'user',
                component: UserPage,
                props: true
            },
            {
                path: '/school-classes/:periodId(\\d+)/:schoolId(\\d+)',
                name: 'school-classes',
                component: SchoolClassesPage,
                props: true
            },
            {
                path: '/school-page/:schoolId(\\d+)',
                name: 'school-page',
                component: SchoolPage,
                props: true
            },
            {
                path: '/school-class-plan/:schoolId(\\d+)/:periodId(\\d+)/:schoolPlanId(\\d+)',
                name: 'school-class-plan',
                component: SchoolClassesPlanPage,
                props: true
            },
            {
                path: '/school-classes-plans/:schoolId(\\d+)/:periodId(\\d+)',
                name: 'school-class-plans',
                component: SchoolClassesPlansPage,
                props: true
            },
            {
                path: '/school-class/:periodId/:schoolId/:schoolClassId',
                name: 'school-class',
                component: SchoolClassPage,
                props: true,
                children: [
                    {
                        path: 'students',
                        component: SchoolClassStudentsTab
                    },
                    {
                        path: 'subjects',
                        component: SchoolClassSubjectsTab
                    },
                    {
                        path: 'plan',
                        component: SchoolClassPlanTab
                    },
                    {
                        path: 'program',
                        component: SchoolClassLessonsTab
                    },
                    {
                        path: 'grades',
                        component: SchoolClassGradesTab
                    },
                    {
                        path: 'remarks',
                        component: SchoolClassRemarksTab
                    },
                    {
                        path: 'absences',
                        component: SchoolClassAbsencesTab
                    },

                ],
            },
            {
                path: '/calendar/:periodId(\\d+)/:schoolId(\\d+)',
                component: CalendarPage,
                name: 'calendar',
                props: true
            },
            {
                path: '/teacher-lessons/:periodId(\\d+)/:schoolId(\\d+)/:teacherId(\\d+)',
                component: TeacherLessonsPage,
                name: 'teacher-lessons',
                props: true
            },
            {
                path: '/program/:periodId(\\d+)/:schoolId(\\d+)',
                component: ProgramPage,
                name: 'program',
                props: true
            },
            {
                path: '/school-class',
                redirect: to => {
                    return {path: `/school-class/:schoolClassId(\\d+)/:periodId(\\d+)/:schoolId(\\d+)/students`}
                },
            },
            {
                path: '/student-diary/:schoolClassId(\\d+)/:studentId(\\d+)/:periodId(\\d+)/:schoolId(\\d+)',
                component: StudentSubjectsAndEvaluation,
                props: true,
                children: [
                    {
                        path: 'grades',
                        component: GradesTabPerStudent
                    },
                    {
                        path: 'absences',
                        component: AbsenceTabPerStudent
                    },
                    {
                        path: 'feedbacks',
                        component: FeedbackTabPerStudent
                    },
                    {
                        path: 'program',
                        component: SchoolClassLessonsTab
                    },
                ],
            },
            {
                path: '/subject-diary/:schoolClassId(\\d+)/:subjectId(\\d+)/:periodId(\\d+)/:schoolId(\\d+)',
                component: SubjectEvaluationsPerSchoolClass,
                props: true,
                children: [
                    {
                        path: 'grades',
                        component: SubjectGradesTab
                    },
                    {
                        path: 'absences',
                        component: SubjectAbsenceTab
                    },
                    {
                        path: 'feedbacks',
                        component: SubjectFeedbackTab
                    }
                ],
            },
            {
                path: '/school-lesson/:periodId(\\d+)/:schoolId(\\d+)/:schoolLessonId(\\d+)',
                component: SchoolLessonPage,
                props: true,
                children: [
                    {
                        path: 'lesson',
                        component: SchoolLessonInformation
                    },
                    {
                        path: 'grades',
                        component: SubjectGradesTab
                    },
                    {
                        path: 'absences',
                        component: SubjectAbsenceTab
                    },
                    {
                        path: 'feedbacks',
                        component: SubjectFeedbackTab
                    }
                ],
            },
            {
                path: '/requests/:periodId(\\d+)/:schoolId(\\d+)', component: RequestsPage, props: true,
                children: [
                    {
                        path: 'user-requests',
                        component: UserRequestsTab
                    },
                    {
                        path: 'role-requests',
                        component: RoleRequestsTab
                    },
                    {
                        path: 'user-status-change-request',
                        component: UserRequestsTab
                    },
                    {
                        path: 'role-status-change-request',
                        component: RoleRequestsTab
                    }
                ],
            }
        ]
    },
    {
        path: '/component',
        redirect: to => ({path: `/component/${new Date().getFullYear()}/${new Date().getMonth()}`}),
    },
    {
        path: '/requests',
        redirect: to => {
            return {path: `/requests/${periodId.value}/${schoolId.value}`}
        },
    },
    {
        path: '/users',
        redirect: to => {
            return {path: `/users/${periodId.value}/${schoolId.value}`}
        },
    },
    {
        path: '/school-classes',
        redirect: to => {
            return {path: `/school-classes/${periodId.value}/${schoolId.value}`}
        },
    },
    {
        path: '/calendar',
        redirect: to => {
            return {path: `/calendar/${periodId.value}/${schoolId.value}`}
        },
    },
    {
        path: '/program',
        redirect: to => {
            return {path: `/program/${periodId.value}/${schoolId.value}`}
        },
    },
]


export const router = createRouter({
    history: createWebHistory(),
    routes,
})
