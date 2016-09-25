use "pusharray.sml";
use "sequence.sml";

fun checkeq seq reflist =
  let
    fun helper idx [] = true
      | helper idx (x::xs) =
        ((Sequence.get seq idx) = x) andalso helper (idx + 1) xs
  in
    helper 0 reflist
  end

val A = Sequence.seq 5 0
val true = checkeq A [0,0,0,0,0]

val B = Sequence.set A 0 1
val true = checkeq B [1,0,0,0,0]

val C = Sequence.set A 0 2
val true = checkeq C [2,0,0,0,0]

val D = Sequence.set A 4 5
val true = checkeq D [0,0,0,0,5]

val E = Sequence.set B 0 5
val true = checkeq E [5,0,0,0,0]

val F = Sequence.set B 2 7
val true = checkeq F [1,0,7,0,0]

val G = Sequence.set F 3 8
val true = checkeq G [1,0,7,8,0]
val true = checkeq B [1,0,0,0,0]