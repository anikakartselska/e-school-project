<template>
  <q-layout view="lHh LpR lFf">
    <q-header
            reveal
            :class="$q.dark.isActive ? 'header_dark' : 'header_normal'"
    >
      <q-toolbar>
        <q-btn
                @click="left = !left"
                flat
                round
                dense
                icon="menu"
                class="q-mr-sm"
        />
        <q-toolbar-title>Електронен дневник</q-toolbar-title>
        <q-btn
                class="q-mr-xs"
                flat
                round
                @click="$q.dark.toggle()"
                :icon="$q.dark.isActive ? 'nights_stay' : 'wb_sunny'"
        />
        <q-btn flat round dense icon="search" class="q-mr-xs"/>
        <q-btn
                flat
                round
                dense
                icon="logout"
                to="/"
        />
      </q-toolbar>
    </q-header>
    <q-drawer
            class="left-navigation text-white"
            show-if-above
            v-model="left"
            style="background-image: url('../src/assets/schoolMenuBackground.png'); background-size: cover;"
            side="left"
            elevated
    >
      <div
              class="full-height drawer_normal"
      >
        <div style="height: calc(100% - 117px);padding:10px;">
          <q-toolbar>
            <q-avatar>
              <img src="https://cdn.quasar.dev/img/boy-avatar.png"/>
            </q-avatar>
            <q-toolbar-title>Mayank Patel</q-toolbar-title>
          </q-toolbar>
          <hr/>
          <q-scroll-area style="height:100%;">
            <q-list v-for="page in pages" padding>
              <q-item
                      active-class="tab-active"
                      :to="page.to"
                      exact
                      class="navigation-item"
                      clickable
                      v-ripple
              >
                <q-item-section avatar>
                  <q-icon name="dashboard"/>
                </q-item-section>
                <q-item-section>
                  {{ page.label }}
                </q-item-section>
              </q-item>
            </q-list>
          </q-scroll-area>
        </div>
      </div>
    </q-drawer>
    <q-page-container>
      <router-view v-slot="{ Component }">
        <template v-if="Component">
          <suspense>
            <component :is="Component"></component>
            <template #fallback>
              <div class="fixed-center">
                <q-spinner
                        :thickness="2"
                        color="primary"
                        size="5.5em"
                />
              </div>
            </template>
          </suspense>
        </template>
      </router-view>
    </q-page-container>
  </q-layout>
</template>

<script lang="ts" setup>
import {$ref} from "vue/macros";

const left = $ref(true)
const pages = [{to: "/users", label: "Потребители", show: true},
  {to: "/dashboard_v2", label: "Dashboard v2", show: true},
  {to: "/dashboard_v3", label: "Dashboard v3", show: true},
  {to: "/customer_management", label: "Customer Management", show: true},
  {to: "/change_request", label: "Change Request", show: true},
  {to: "/sales_invoices", label: "Sales Invoices", show: true},
  {to: "/quotes", label: "Quotes", show: true},
  {to: "/transactions", label: "Transactions", show: true},
  {to: "/employee_salary_list", label: "Employee Salary List", show: true},
  {to: "/calendar", label: "Calendar", show: true},
  {
    to: "/department", label: "Department", show: true
  }]

</script>


<style>
.drawer_normal {
  background-color: rgba(1, 1, 1, 0.75);
}

.drawer_dark {
  background-color: #010101f2;
}

.navigation-item {
  border-radius: 5px;
}

.tab-active {
  background-color: green;
}

body {
  background: #f1f1f1 !important;
}

.header_normal {
  background: linear-gradient(
          145deg,
          rgb(32, 106, 80) 15%,
          rgb(21, 57, 102) 70%
  );
}

.header_dark {
  background: linear-gradient(145deg, rgb(61, 14, 42) 15%, rgb(14, 43, 78) 70%);
}
</style>
