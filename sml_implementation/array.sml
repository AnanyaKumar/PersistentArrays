structure Farray = struct
  type 'a arraydata = (int ref) * 'a array * ('a PushArray.t) array

  type 'a t = int * 'a arraydata

  fun farray size init =
    (1, (ref 1, 
    Array.array(size, init), 
    Array.tabulate(size, fn x => PushArray.pusharray (0, init))))

  fun size (v, (ref v', vals, logs)) = Array.length(vals)

  fun copyad (v, (ref vr, vals, logs)) =
    (ref vr,
    Array.tabulate(Array.length(vals), fn i => 
      get ((v, (ref vr, vals, logs))) i),
    Array.tabulate(Array.length(vals), 
      fn i => PushArray.pusharray (0, Array.sub(vals,0))))

  fun cmpswap addr oldval newval =
    if !addr = oldval then (addr := newval; true) else false

  fun set (v, (vr, vals, logs)) i value =
    if not(!vr mod (10 * (Array.length vals)) = 0) andalso cmpswap vr v (v+1) then
      (PushArray.push (Array.sub(logs, i)) (v, Array.sub(vals, i));
      Array.update(vals, i, value);
      (v+1, (vr, vals, logs)))
    else
      let 
        val (vr', vals', logs') = copyad (v, (vr, vals, logs))
      in 
        Array.update(vals', i, value); (v, (ref v, vals', logs'))
      end

  fun binrecur log version lower upper =
    let 
      val mid = (lower + upper) div 2
      val (v, value) = PushArray.get log mid
    in 
      if lower = upper then value
      else if v < version then binrecur log version (mid+1) upper
      else binrecur log version lower mid
    end

  fun binsearch log version = binrecur log version 0 ((PushArray.size log) - 1)

  fun get (v, (vr, vals, logs)) i =
    let 
      val guess = Array.sub(vals, i) 
      val curlog = Array.sub(logs, i)
      val loglen = PushArray.size curlog
    in 
      if v = !vr orelse loglen = 0 then guess
      else 
        let 
          val (lastversion, _) = PushArray.get curlog (loglen - 1)
        in 
          if lastversion < v then guess
          else binsearch curlog v
        end
    end
end
