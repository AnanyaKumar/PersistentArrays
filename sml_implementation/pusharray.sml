structure PushArray = struct
  type 'a t = (int * int * ('a array)) ref

  fun pusharray init = ref(10, 0, Array.array(10,init))

  fun push (rwa : 'a t) (v : 'a) =
    let 
      val ref(cap, size, data) = rwa
    in
      if cap = size then 
        let 
          val newcap = 2 * cap
          val newdata = Array.array(newcap, Array.sub(data, 0))
        in 
          Array.copy{src = data, dst = newdata, di = 0};
          Array.update(newdata, size, v);
          rwa := (newcap, size+1, newdata)
        end
      else 
        (Array.update(data, size, v);
        rwa := (cap, size+1, data))
    end

    fun size (ref(cap, size, data)) = size

    fun get (ref(cap, size, data)) idx = Array.sub(data, idx)
end
