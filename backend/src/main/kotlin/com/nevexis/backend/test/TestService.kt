package com.nevexis.backend.test

import org.springframework.stereotype.Service
import java.util.stream.Collectors
import kotlin.random.Random
import kotlin.streams.asSequence

@Service
class TestService {


    val POPULATION_SIZE: Int = 9
    val MUTATION_RATE: Double = 0.1
    val CROSSOVER_RATE: Double = 0.9
    val TOURNAMENT_SELECTION_SIZE: Int = 3
    val NUM_OF_ELITE_SCHEDULES: Int = 2


    enum class WorkingDays(val order: Int) {
        MONDAY(1),
        TUESDAY(2),
        WEDNESDAY(3),
        THURSDAY(4),
        FRIDAY(5)
    }

    data class Room(
        val number: String,
        val capacity: Int
    )

    data class Teacher(
        val name: String,
        val subjects: Set<String>,
        val workingHours: Int
    )

    data class SchoolClass(
        val name: String,
        val subjectToClassesCount: Map<String, Int>
    )

    data class WorkingHour(
        val workingDays: WorkingDays,
        val hour: Int
    )

    data class Schedule(
        val schoolLessons: List<SchoolLesson>,
        val fitness: Double
    )

    data class Population(
        val schedules: List<Schedule>
    )

    val workingHours = WorkingDays.values().map { workingDay ->
        (1..7).toList().map { hour ->
            WorkingHour(
                workingDay,
                hour
            )
        }
    }.flatten()
    val rooms = listOf(
        Room("101", 15),
        Room("102", 15),
        Room("103", 15),
        Room("104", 15),
        Room("105", 15),
        Room("201", 15),
        Room("202", 15),
        Room("203", 15),
        Room("204", 15),
        Room("205", 15),
        Room("301", 30),
        Room("302", 30),
        Room("303", 30),
        Room("304", 30),
        Room("305", 30),
        Room("401", 30),
        Room("402", 30),
        Room("403", 30),
        Room("404", 30),
        Room("405", 30),
        Room("501", 30),
        Room("502", 30),
        Room("503", 30),
        Room("504", 30),
        Room("505", 30)
    )
    val subjects = listOf(
        "Mathematics",
        "Physics",
        "Chemistry",
        "Biology",
        "English",
        "Literature",
        "History",
        "Geography",
        "Art",
        "Design",
        "Computer Science",
        "Information Technology",
        "Physical Education",
        "Health Education",
        "Music",
        "Theater",
        "Economics",
        "Business Studies",
        "Psychology",
        "Sociology"
    )
    val teacher1 = Teacher("teacher1", setOf("Mathematics", "Physics"), 40)
    val teacher2 = Teacher("teacher2", setOf("Chemistry", "Biology"), 40)
    val teacher3 = Teacher("teacher3", setOf("English", "Literature"), 40)
    val teacher4 = Teacher("teacher4", setOf("History", "Geography"), 40)
    val teacher5 = Teacher("teacher5", setOf("Art", "Design"), 40)
    val teacher6 = Teacher("teacher6", setOf("Computer Science", "Information Technology"), 40)
    val teacher7 = Teacher("teacher7", setOf("Physical Education", "Health Education"), 40)
    val teacher8 = Teacher("teacher8", setOf("Music", "Theater"), 40)
    val teacher9 = Teacher("teacher9", setOf("Economics", "Business Studies"), 40)
    val teacher10 = Teacher("teacher10", setOf("Psychology", "Sociology"), 40)
    val teacher11 = Teacher("teacher11", setOf("Mathematics", "Physics"), 40)
    val teacher12 = Teacher("teacher12", setOf("Chemistry", "Biology"), 40)
    val teacher13 = Teacher("teacher13", setOf("English", "Literature"), 40)
    val teacher14 = Teacher("teacher14", setOf("History", "Geography"), 40)
    val teacher15 = Teacher("teacher15", setOf("Art", "Design"), 40)
    val teacher16 = Teacher("teacher16", setOf("Computer Science", "Information Technology"), 40)
    val teacher17 = Teacher("teacher17", setOf("Physical Education", "Health Education"), 40)
    val teacher18 = Teacher("teacher18", setOf("Music", "Theater"), 40)
    val teacher19 = Teacher("teacher19", setOf("Economics", "Business Studies"), 40)
    val teacher20 = Teacher("teacher20", setOf("Psychology", "Sociology"), 40)

    // List of teachers
    val teachersList =
        listOf(
            teacher1,
            teacher2,
            teacher3,
            teacher4,
            teacher5,
            teacher6,
            teacher7,
            teacher8,
            teacher9,
            teacher10,
            teacher11,
            teacher12,
            teacher13,
            teacher14,
            teacher15,
            teacher16,
            teacher17,
            teacher18,
            teacher19,
            teacher20
        )

    val schoolClasses = listOf(
        SchoolClass(
            "9a", mapOf(
                "Mathematics" to 5,
                "Physics" to 3,
                "Chemistry" to 4,
                "Biology" to 3,
                "English" to 6,
                "Literature" to 2,
                "History" to 4,
                "Geography" to 2,
                "Art" to 2,
                "Design" to 1,
                "Computer Science" to 3,
                "Information Technology" to 2,
                "Physical Education" to 2,
                "Health Education" to 1,
                "Music" to 2,
                "Theater" to 1,
                "Economics" to 3,
                "Business Studies" to 2,
                "Psychology" to 2,
                "Sociology" to 1
            )
        ), SchoolClass(
            "10a", mapOf(
                "Mathematics" to 6,
                "Physics" to 4,
                "Chemistry" to 5,
                "Biology" to 4,
                "English" to 7,
                "Literature" to 3,
                "History" to 5,
                "Geography" to 3,
                "Art" to 3,
                "Design" to 2,
                "Computer Science" to 4,
                "Information Technology" to 3,
                "Physical Education" to 3,
                "Health Education" to 2,
                "Music" to 3,
                "Theater" to 2,
                "Economics" to 4,
                "Business Studies" to 3,
                "Psychology" to 3,
                "Sociology" to 2,
            )
        ),
        SchoolClass(
            "11a", mapOf(
                "Mathematics" to 6,
                "Physics" to 4,
                "Chemistry" to 5,
                "Biology" to 4,
                "English" to 7,
                "Literature" to 3,
                "History" to 5,
                "Geography" to 3,
                "Art" to 3,
                "Design" to 2,
                "Computer Science" to 4,
                "Information Technology" to 3,
                "Physical Education" to 3,
                "Health Education" to 2,
                "Music" to 3,
                "Theater" to 2,
                "Economics" to 4,
                "Business Studies" to 3,
                "Psychology" to 3,
                "Sociology" to 2,
            )
        ),
        SchoolClass(
            "9b", mapOf(
                "Mathematics" to 5,
                "Physics" to 3,
                "Chemistry" to 4,
                "Biology" to 3,
                "English" to 6,
                "Literature" to 2,
                "History" to 4,
                "Geography" to 2,
                "Art" to 2,
                "Design" to 1,
                "Computer Science" to 3,
                "Information Technology" to 2,
                "Physical Education" to 2,
                "Health Education" to 1,
                "Music" to 2,
                "Theater" to 1,
                "Economics" to 3,
                "Business Studies" to 2,
                "Psychology" to 2,
                "Sociology" to 1
            )
        )
    )

    data class SchoolLesson(
        val room: Room,
        val workingHour: WorkingHour,
        val teacher: Teacher,
        val subject: String,
        val schoolClass: SchoolClass
    ) {
        override fun toString(): String {
            return "SchoolLesson(room=$room, day=${workingHour.workingDays}, hour=${workingHour.hour}, teacher=${teacher.name}, subject='$subject', schoolClass=${schoolClass.name})"
        }
    }


    val subjectToTeachersTeachingIt = subjects.associateWith { subject ->
        teachersList.filter { teacher -> teacher.subjects.contains(subject) }
    }

    fun customNextInt(from: Int, until: Int): Int {
        return if (from == until) {
            from
        } else {
            Random.nextInt(from, until)
        }
    }

    fun testProgram() =
        schoolClasses.parallelStream().asSequence().map { schoolClass ->
            val allSubjectHoursCount = schoolClass.subjectToClassesCount.values.sum()
            val maxClassesADay = kotlin.math.ceil(allSubjectHoursCount / 5.0).toInt()
            val busySubjects = schoolClass.subjectToClassesCount.toMutableMap()
            val workingHoursList = WorkingDays.values().map { workingDay ->
                (1..maxClassesADay).toList().map { hour ->
                    WorkingHour(
                        workingDay,
                        hour
                    )
                }
            }.flatten().toMutableList()

            (1..allSubjectHoursCount).toList().map {
                val availableSubjects =
                    busySubjects.filter { (_, count) -> count > 0 }

                val subject = availableSubjects.keys.toList()[
                    customNextInt(
                        0, availableSubjects.size - 1
                    )]

                busySubjects[subject] = busySubjects[subject]!! - 1

                val workingHour = workingHoursList.toList()[
                    customNextInt(
                        0, workingHoursList.size - 1
                    )]
                workingHoursList.remove(workingHour)


                SchoolLesson(
                    room = rooms[customNextInt(0, rooms.size - 1)],
                    workingHour = workingHour,
                    teacher = subjectToTeachersTeachingIt[subject]!![customNextInt(
                        0,
                        subjectToTeachersTeachingIt[subject]!!.size - 1
                    )],
                    subject = subject,
                    schoolClass = schoolClass,
                )
            }
        }.flatten().toList()

    fun calculateFitness(schoolLessons: List<SchoolLesson>): Double {
        val schoolLessonsGroupedByTeacherAndTime = schoolLessons.groupBy { Pair(it.teacher, it.workingHour) }
        val schoolLessonsGroupedByRoom = schoolLessons.groupBy { Pair(it.room, it.workingHour) }

        val schoolLessonsGroupedBySchoolClassAndSubject = schoolLessons.groupBy { Pair(it.schoolClass, it.subject) }
            .mapValues { (_, lessons) ->
                if (lessons.map { it.teacher }.distinct().size == 1) {
                    0
                } else {
                    1
                }
            }.values.sum()

        val repeatableTeacherClasses = schoolLessonsGroupedByTeacherAndTime.map { (_, values) ->
            values.size - 1
        }.sum()
        val repeatableRooms = schoolLessonsGroupedByRoom.map { (_, values) ->
            values.size - 1
        }.sum()

        val numberOfConflicts = repeatableTeacherClasses + repeatableRooms + schoolLessonsGroupedBySchoolClassAndSubject
        return 1 / (numberOfConflicts + 1.0)
    }

    fun populate(): Population {
        return Population((1..50).toList().parallelStream()
            .map {
                val schedule = testProgram()
                Schedule(schedule, calculateFitness(schedule))
            }.collect(Collectors.toList())
            .sortedByDescending { it.fitness })

    }

    private val uniformRate = 0.5
    private val mutationRate = 0.015
    private val tournamentSize = 5
    private val elitism = true

    fun evolvePopulation(pop: Population): Population {
        return Population((0 until pop.schedules.size).toList().parallelStream().map {
            if (it < 10) {
                pop.schedules.first()
            } else {
                val indiv1 = tournamentSelection(pop)
                val indiv2 = tournamentSelection(pop)
                val newIndiv = crossover(indiv1, indiv2)
                mutate(newIndiv)
            }
        }.collect(Collectors.toList())
            .sortedByDescending { it.fitness })

    }

    fun crossover(schedule1: Schedule, schedule2: Schedule): Schedule {
        val schoolLessons = (0 until schedule1.schoolLessons.size).toList().parallelStream().map {
            if (Math.random() <= uniformRate) {
                schedule1.schoolLessons[it]
            } else {
                schedule2.schoolLessons[it]
            }
        }.collect(Collectors.toList())
        return Schedule(schoolLessons, calculateFitness(schoolLessons))
    }

    fun mutate(schedule: Schedule): Schedule {
        // Loop through genes
        val newSchedule = testProgram()
        val schoolLessons = (0 until schedule.schoolLessons.size).toList().parallelStream().map {
            if (Math.random() <= mutationRate) {
                newSchedule[it]
            } else {
                schedule.schoolLessons[it]
            }
        }.collect(Collectors.toList())
        return Schedule(schoolLessons, calculateFitness(schoolLessons))
    }

    fun tournamentSelection(pop: Population): Schedule {
        return (0 until tournamentSize).toList().map {
            pop.schedules[(Math.random() * pop.schedules.size).toInt()]
        }.maxBy { it.fitness }
    }

    //fun crossoverPopulation(population: Population): Population {
//    return Population(
//        List(population.schedules.size) { index ->
//            if (index < NUM_OF_ELITE_SCHEDULES) {
//                population.schedules[index]
//            } else {
//                val schedule1 = selectTournamentPopulation(population).schedules.first()
//                val schedule2 = selectTournamentPopulation(population).schedules.first()
//                crossoverSchedule(schedule1, schedule2)
//            }
//        }.sortedByDescending { it.fitness })
//}
//
//fun crossoverSchedule(schedule1: Schedule, schedule2: Schedule): Schedule {
//    val crossoverScheduleSize = max(schedule1.schoolLessons.size, schedule2.schoolLessons.size)
//    val lessons = (0 until crossoverScheduleSize).map { index ->
//        if (Math.random() > 0.5)
//            schedule1.schoolLessons[index]
//        else
//            schedule2.schoolLessons[index]
//    }
//    return Schedule(lessons, calculateFitness(lessons))
//}
//
//fun selectTournamentPopulation(population: Population): Population {
//    return Population((0..3).toList().map {
//        customNextInt(0, population.schedules.size - 1)
//    }.map { population.schedules[it] }.sortedByDescending { it.fitness })
//}
//
//fun mutatePopulation(population: Population): Population {
//    return Population(population.schedules.mapIndexed { index, schedule ->
//        if (index < NUM_OF_ELITE_SCHEDULES) {
//            schedule
//        } else {
//            mutateSchedule(schedule)
//        }
//    }.sortedByDescending { it.fitness })
//}
//
//fun mutateSchedule(mutateSchedule: Schedule): Schedule {
//    val schoolLessons = testProgram().mapIndexed { index, lesson ->
//        if (MUTATION_RATE > Math.random()) {
//            lesson
//        } else {
//            mutateSchedule.schoolLessons[index]
//        }
//    }
//
//    return Schedule(schoolLessons, calculateFitness(schoolLessons))
//}
//
//fun evolve(population: Population): Population {
//    return mutatePopulation(crossoverPopulation(population))
//}
//
    fun run() {
        var population = populate()

        while (population.schedules.first().fitness < 1) {
            population = evolvePopulation(population)
            println(population.schedules.first().fitness)
        }


    }

}