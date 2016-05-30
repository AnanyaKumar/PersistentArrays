structure Farray = struct
  type 'a arraydata = (int ref) * 'a array * ('a RWArray.t) array

  type 'a t = int * 'a arraydata

  fun farray size init =
    (1, (ref 1, 
    Array.array(size, init), 
    Array.tabulate(size, fn x => RWArray.rwarray (0, init))))

  fun size (v, (ref v', vals, logs)) = Array.length(vals)

  fun copyad (ref v, vals, logs) =
    (ref v,
    Array.tabulate(Array.length(vals), fn i => Array.sub(vals, i)),
    Array.tabulate(Array.length(vals), 
      fn i => RWArray.rwarray (0, Array.sub(vals,0))))

  fun cmpswap addr oldval newval =
    if !addr = oldval then (addr := newval; true) else false

  fun set (v, (vr, vals, logs)) i value =
    if not(!vr mod (10 * (Array.length vals)) = 0) andalso cmpswap vr v (v+1) then
      (RWArray.push (Array.sub(logs, i)) (v, Array.sub(vals, i));
      Array.update(vals, i, value);
      (v+1, (vr, vals, logs)))
    else
      let 
        val (vr', vals', logs') = copyad (vr, vals, logs)
      in 
        Array.update(vals', i, value); (v, (ref v, vals', logs'))
      end

  fun binrecur log version lower upper =
    let 
      val mid = (lower + upper) div 2
      val (v, value) = RWArray.get log mid
    in 
      if lower = upper then value
      else if v < version then binrecur log version (mid+1) upper
      else binrecur log version lower mid
    end

  fun binsearch log version = binrecur log version 0 ((RWArray.size log) - 1)

  fun get (v, (vr, vals, logs)) i =
    let 
      val guess = Array.sub(vals, i) 
      val curlog = Array.sub(logs, i)
      val loglen = RWArray.size curlog
    in 
      if v = !vr orelse loglen = 0 then guess
      else 
        let 
          val (lastversion, _) = RWArray.get curlog (loglen - 1)
        in 
          if lastversion < v then guess
          else binsearch curlog v
        end
    end
end
