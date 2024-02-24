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
        redirect: to => ({path: `/home`}),
        children: [
            {path: '/home', component: Home},
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
    }

]


export const router = createRouter({
    history: createWebHistory(),
    routes,
})
