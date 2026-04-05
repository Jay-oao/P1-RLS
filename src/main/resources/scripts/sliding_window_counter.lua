-- GET current window count
-- GET previous window count
-- CALC weight
-- CALC effective count
-- CHECK allow/reject
-- INCR current window (if allowed)
-- EXPIRE keys
-- RETURN

-- KEYS[1] = currentWindowKey
-- KEYS[2] = previousWindowKey

-- ARGV[1] = limit
-- ARGV[2] = windowMs
-- ARGV[3] = nowMs

local currKey = KEYS[1]
local prevKey = KEYS[2]

local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

local curr = tonumber(redis.call("GET", currKey)) or 0
local prev = tonumber(redis.call("GET", prevKey)) or 0

local windowStart = now - (now % window)
local elapsed = now - windowStart

local weight = (window - elapsed) / window

local effective = curr + (prev * weight)

local allowed = 0
local retryAfterMs = 0

if effective < limit then
	allowed = 1
	curr = redis.call("INCR", currKey)
else
	retryAfterMs = window - elapsed
end

local ttl = math.ceil((window * 2) / 1000)
redis.call("EXPIRE", currKey, ttl)
redis.call("EXPIRE", prevKey, ttl)

local remaining = math.max(0, limit - effective)

return {allowed, remaining, retryAfterMs}