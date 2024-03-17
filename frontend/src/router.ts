import MainPage from './pages/MainPage.vue'
import Home from './pages/Home.vue'
import Component from './pages/Component.vue'
import {createRouter, createWebHistory} from "vue-router";
import UsersPage from "./pages/users-page.vue";
import UserPage from "./pages/user-page.vue";
import AbsenceTabPerStudent from "./pages/per-student-evaluations/absence-tab.vue";
import FeedbackTabPerStudent from "./pages/per-student-evaluations/feedback-tab.vue";
import FeedbackTabPerSubject from "./pages/per-school-class-evaluations/feedback-tab.vue";
import StudentSubjectsAndEvaluation from "./pages/per-student-evaluations/student-subjects-and-evaluation.vue";
import SchoolClassSubjectsAndEvaluations
    from "./pages/per-school-class-evaluations/school-class-subjects-and-evaluations.vue";
import GradesTabPerSubject from "./pages/per-school-class-evaluations/grades-tab.vue";
import AbsenceTabPerSubject from "./pages/per-school-class-evaluations/absence-tab.vue";
import LoginSingIn from "./pages/login-signin.vue";
import LoginTab from "./pages/login-tab.vue";
import SignInTab from "./pages/sign-in-tab.vue";
import {userIsLoggedIn} from "./services/LocalStorageService";
import RequestsPage from "./pages/requests/requests-page.vue";
import {periodId, schoolId} from "./model/constants";
import UserRequestsTab from "./pages/requests/user-requests-tab.vue";
import RoleRequestsTab from "./pages/requests/role-requests-tab.vue";

const routes = [
    {
        path: '/login-sign-in', component: LoginSingIn, props: false,
        children: [
            {path: '/login', component: LoginTab, name: "login"},
            {path: '/sign-in', component: SignInTab, name: "sign-in"},
        ]
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
            {path: '/users', component: UsersPage, name: 'users'},
            {
                path: '/user/:id(\\d+)/:periodId(\\d+)/:schoolId(\\d+)',
                name: 'user',
                component: UserPage,
                props: true
            },
            {
                path: '/diary/:schoolClassId(\\d+)/:studentId(\\d+)/:periodId(\\d+)/:schoolId(\\d+)',
                component: StudentSubjectsAndEvaluation,
                props: true,
                children: [
                    {
                        path: 'grades',
                        component: FeedbackTabPerStudent
                    },
                    {
                        path: 'absences',
                        component: AbsenceTabPerStudent
                    },
                    {
                        path: 'feedbacks',
                        component: FeedbackTabPerStudent
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
                    }
                ],
            },
            {
                path: '/schoolClassDiary/:schoolClassId(\\d+)/:periodId(\\d+)/:schoolId(\\d+)',
                component: SchoolClassSubjectsAndEvaluations,
                props: true,
                children: [
                    {
                        path: 'grades',
                        component: GradesTabPerSubject
                    },
                    {
                        path: 'absences',
                        component: AbsenceTabPerSubject
                    },
                    {
                        path: 'feedbacks',
                        component: FeedbackTabPerSubject
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

]


export const router = createRouter({
    history: createWebHistory(),
    routes,
})
