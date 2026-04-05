-- REMOVE old entries
-- COUNT current
-- CHECK allow/reject
-- ADD new entry (if allowed)
-- EXPIRE
-- RETURN

-- KEYS[1] = logKey

-- ARGV[1] = limit
-- ARGV[2] = windowMs
-- ARGV[3] = nowMs

local key = KEYS[1]

local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

local windowStart = now - window

redis.call("ZREMRANGEBYSCORE", key, 0, windowStart)

local current = redis.call("ZCARD", key)

local allowed = 0
local retryAfterMs = 0

if current < limit then
	allowed = 1

	redis.call("ZADD", key, now, now)

	current = current + 1
else
	local oldest = redis.call("ZRANGE", key, 0, 0, "WITHSCORES")
	if oldest[2] ~= nil then
		retryAfterMs = window - (now - tonumber(oldest[2]))
	end
end

redis.call("EXPIRE", key, math.ceil(window / 1000))

local remaining = math.max(0, limit - current)

return {allowed, remaining, retryAfterMs}