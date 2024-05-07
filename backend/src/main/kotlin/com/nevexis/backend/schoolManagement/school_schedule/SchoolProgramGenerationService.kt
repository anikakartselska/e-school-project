package com.nevexis.backend.schoolManagement.school_schedule

import com.nevexis.backend.schoolManagement.BaseService
import com.nevexis.backend.schoolManagement.school_class.SchoolClassWithPlan
import com.nevexis.backend.schoolManagement.users.TeacherView
import org.springframework.stereotype.Service
import java.util.stream.Collectors
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.random.Random

@Service
class SchoolProgramGenerationService : BaseService() {

    private val uniformRate = 0.5
    private val mutationRate = 0.015
    private val tournamentSize = 5


    fun generatePlannedSchoolLessonsForEachClass(
        teachers: List<TeacherView>,
        schoolClasses: List<SchoolClassWithPlan>,
        subjects: List<String>,
        rooms: List<String>
    ): List<PlannedSchoolLesson> {
        var population = populate(teachers, schoolClasses, subjects, rooms)

        while (population.schedules.first().fitness < 1) {
            population = evolvePopulation(population, teachers, schoolClasses, subjects, rooms)
            println(population.schedules.first().fitness)
        }
        return population.schedules.first().planedSchoolLessons
    }

    private fun generateProgram(
        teachersList: List<TeacherView>,
        schoolClasses: List<SchoolClassWithPlan>,
        subjects: List<String>,
        rooms: List<String>
    ): List<PlannedSchoolLesson> {
        val subjectToTeachersTeachingIt = subjects.associateWith { subject ->
            teachersList.filter { teacher -> teacher.qualifiedSubjects.contains(subject) }
        }

        val days = WorkingDays.values().toMutableList()

        return schoolClasses.asSequence().map { schoolClass ->
            val allSubjectHoursCount = schoolClass.plan.values.sum()
            val maxClassesADay = kotlin.math.floor(allSubjectHoursCount / 5.0).toInt() + 1
            val left = allSubjectHoursCount % 5
            val busySubjects = schoolClass.plan.toMutableMap()
            val workingHoursList = WorkingDays.values().map { workingDay ->
                (1..maxClassesADay).toList().map { hour ->
                    WorkingHour(
                        workingDay,
                        hour
                    )
                }
            }.flatten().let {
                val randomDay = customNextInt(1, days.size)
                it.plus(
                    (1..left).toList().map {
                        WorkingHour(
                            days[randomDay],
                            maxClassesADay + 1
                        )
                    }
                ).also {
                    days.removeAt(randomDay)
                }
            }.toMutableList()
            val subjectToTeacherView =
                subjectToTeachersTeachingIt.mapValues { (_, values) -> values[customNextInt(0, values.size - 1)] }

            (1..allSubjectHoursCount).toList().map {
                val availableSubjects =
                    busySubjects.filter { (_, count) -> count > 0 }

                val subject = availableSubjects.keys.toList()[
                    customNextInt(
                        0, availableSubjects.size - 1
                    )]

                busySubjects[subject] = busySubjects[subject]!! - 1

                val workingHour = workingHoursList[
                    customNextInt(
                        0, workingHoursList.size - 1
                    )]
                workingHoursList.remove(workingHour)


                PlannedSchoolLesson(
                    room = rooms[customNextInt(0, rooms.size - 1)],
                    workingHour = workingHour,
                    teacher = subjectToTeacherView[subject]!!,
                    subject = subject,
                    schoolClass = schoolClass,
                )
            }
        }.flatten().toList()
    }

    private fun calculateFitness(planedSchoolLessons: List<PlannedSchoolLesson>): Double {
        val schoolLessonsGroupedByTeacherAndTime = planedSchoolLessons.groupBy { Pair(it.teacher, it.workingHour) }
        val schoolLessonsGroupedBySchoolClassAndTime =
            planedSchoolLessons.groupBy { Pair(it.schoolClass, it.workingHour) }
        val schoolLessonsGroupedBySchoolClassAndDay =
            planedSchoolLessons.groupBy { Pair(it.schoolClass, it.workingHour.workingDays) }
        val schoolLessonsGroupedByRoom = planedSchoolLessons.groupBy { Pair(it.room, it.workingHour) }

        val schoolLessonsGroupedBySchoolClassAndSubject =
            planedSchoolLessons.groupBy { Pair(it.schoolClass, it.subject) }
        val countOfClasses =
            planedSchoolLessons.groupBy { it.schoolClass }.mapValues { (_, values) -> values.groupBy { it.subject } }
                .map { (key, value) ->
                    value.map { (subject, lessons) ->
                        if (key.plan[subject] == lessons.size) {
                            0
                        } else {
                            1
                        }
                    }
                }.flatten().sum()

        val countOfClassesPerDay = schoolLessonsGroupedBySchoolClassAndDay.map { (key, values) ->
            val averageClassesPerDay = key.first.plan.values.sum() / 5.0
            if (listOf(ceil(averageClassesPerDay), floor(averageClassesPerDay)).contains(values.size.toDouble())) {
                0
            } else {
                1
            }
        }.sum()
//        schoolLessonsGroupedBySchoolClassAndSubject.mapValues { (_, lessons) -> lessons.size }
//            .map { (key, value) ->
//                if (key.first.plan[key.second] == value) {
//                    0
//                } else {
//                    1
//                }
//            }.sum()
        val teacherTeachingSubjectOfTheSameClass =
            schoolLessonsGroupedBySchoolClassAndSubject.mapValues { (_, lessons) ->
                if (lessons.map { it.teacher }.distinct().size == 1) {
                    0
                } else {
                    1
                }
            }.values.sum()

        val repeatableTeacherClasses = schoolLessonsGroupedByTeacherAndTime.map { (_, values) ->
            if (values.size < 2) {
                0
            } else {
                1
            }
        }.sum()
        val repeatableRooms = schoolLessonsGroupedByRoom.map { (_, values) ->
            values.size - 1
        }.sum()
        val repeatableSchoolClassLessons = schoolLessonsGroupedBySchoolClassAndTime.map { (_, values) ->
            values.size - 1
        }.sum()
        val notConsecutiveClasses = schoolLessonsGroupedBySchoolClassAndDay.map { (_, values) ->
            val workingHours = values.map { it.workingHour.hour }
            if (!workingHours.contains(1)) {
                1
            } else {
                if (isConsecutive(workingHours)) {
                    0
                } else {
                    1
                }
            }
        }.sum()

        val numberOfConflicts =
            countOfClasses + repeatableRooms + repeatableSchoolClassLessons + repeatableTeacherClasses + teacherTeachingSubjectOfTheSameClass + countOfClassesPerDay
//             + // + notConsecutiveClasses +
        val fitness = 1 / (numberOfConflicts + 1.0)
        if (fitness > 0.3) {
            //    println()
        }
        return fitness
    }

    private fun populate(
        teachers: List<TeacherView>, schoolClasses: List<SchoolClassWithPlan>, subjects: List<String>,
        rooms: List<String>
    ): Population {
        return Population((1..100).toList().parallelStream()
            .map {
                val schedule = generateProgram(teachers, schoolClasses, subjects, rooms)
                Schedule(schedule, calculateFitness(schedule))
            }.collect(Collectors.toList())
            .sortedByDescending { it.fitness })

    }

    private fun evolvePopulation(
        pop: Population,
        teachers: List<TeacherView>,
        schoolClasses: List<SchoolClassWithPlan>,
        subjects: List<String>,
        rooms: List<String>
    ): Population {
        return Population(
            (0 until pop.schedules.size).toList().parallelStream().map {
                if (it < 10) {
                    pop.schedules.first()
                } else {
                    val indiv1 = tournamentSelection(pop)
                    val indiv2 = tournamentSelection(pop)
                    val newIndiv = crossover(indiv1, indiv2)
                    mutate(
                        newIndiv, teachers, schoolClasses, subjects,
                        rooms
                    )
                }
            }.collect(Collectors.toList())
                .sortedByDescending { it.fitness })

    }

    private fun crossover(schedule1: Schedule, schedule2: Schedule): Schedule {
        val schoolLessons = (0 until schedule1.planedSchoolLessons.size).toList().parallelStream().map {
            if (Math.random() <= uniformRate) {
                schedule1.planedSchoolLessons[it]
            } else {
                schedule2.planedSchoolLessons[it]
            }
        }.collect(Collectors.toList())
        return Schedule(schoolLessons, calculateFitness(schoolLessons))
    }

    private fun mutate(
        schedule: Schedule,
        teachers: List<TeacherView>,
        schoolClasses: List<SchoolClassWithPlan>,
        subjects: List<String>,
        rooms: List<String>
    ): Schedule {
        // Loop through genes
        val newSchedule = generateProgram(
            teachers, schoolClasses, subjects,
            rooms
        )
        val schoolLessons = (0 until schedule.planedSchoolLessons.size).toList().parallelStream().map {
            if (Math.random() <= mutationRate) {
                newSchedule[it]
            } else {
                schedule.planedSchoolLessons[it]
            }
        }.collect(Collectors.toList())
        return Schedule(schoolLessons, calculateFitness(schoolLessons))
    }

    private fun tournamentSelection(pop: Population): Schedule {
        return (0 until tournamentSize).toList().map {
            pop.schedules[(Math.random() * pop.schedules.size).toInt()]
        }.maxBy { it.fitness }
    }

    private fun isConsecutive(numbers: List<Int>): Boolean {
        val sortedList = numbers.sorted()
        for (i in 1 until sortedList.size) {
            if (sortedList[i] != sortedList[i - 1] + 1) {
                return false
            }
        }
        return true
    }

    private fun customNextInt(from: Int, until: Int): Int {
        return if (from == until) {
            from
        } else {
            Random.nextInt(from, until)
        }
    }
}