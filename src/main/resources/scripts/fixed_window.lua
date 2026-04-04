-- GET counter
-- INCR counter
-- EXPIRE (if new key)
-- GET TTL

-- KEYS[1] = redisKey
-- ARGV[1] = windowSeconds

local key = KEYS[1]
local window = tonumber(ARGV[1])

local current = redis.call("INCR", key)

if current == 1 then
  redis.call("EXPIRE", key, window)
end

local ttl = redis.call("TTL", key)

return {current, ttl}