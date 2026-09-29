package com.example.lock_in.data

object Db {
  const val USR = "users"
  const val MEAL="meals"; const val WKT = "workouts"
  const val RTN = "routines"
  const val LOCK ="lock"
  const val CRED = "credits"

  const val FOOD = "foods"
  const val EX = "exercises"
  const val PRE="presets"
}

data class Usr(val uid:String="", val nm:String="", val em:String="",
   val age:Int=0, val sex:String="M", val ht:Double=0.0, val wt:Double=0.0,
   val act:String="MOD", val goal:String="KEEP",
   val kcal:Int=0, val pro:Int=0, val carb:Int=0, val fat:Int=0,
   val onb:Boolean=false, val ts:Long=0)

data class Food(val id: String = "", val nm: String = "", val kcal: Double = 0.0, val pro: Double = 0.0, val carb: Double = 0.0, val fat: Double = 0.0, val srv: Double = 100.0)

data class Meal(
    val id: String = "",
    val day: String = "",
    val slot: String = "",
    val fid: String="",
    val nm: String = "",
    val g: Double = 0.0,
    val kcal: Double = 0.0, val pro: Double = 0.0, val carb: Double = 0.0, val fat: Double = 0.0,
    val ts: Long = 0,
)

data class Ex(val id:String="",val nm:String="",val grp:String="",val eq:String="")
data class ExSt(val eid: String = "", val nm: String = "", val sets: Int = 0, val reps: Int = 0, val wt: Double = 0.0)

data class Rtn(val id: String = "", val nm: String = "", val grp: String = "",
               val pre: Boolean = false, val ex: List<ExSt> = emptyList())
data class Wkt(
  val id: String = "", val rid: String = "", val nm: String = "", val day: String = "",
  val min: Int = 0,
  val ex: List<ExSt> = emptyList(),
  val earn: Int = 0, val ts: Long = 0
)

data class LCfg(val on: Boolean = false, val apps: List<String> = listOf(), val bank: Int = 0, val wkm: Int = 30, val nutm: Int = 15, val ts: Long = 0)


data class Cred(val id: String = "", val amt: Int = 0, val src: String = "", val ref: String = "", val ts: Long = 0)
