--GET level
--GET timestamp
--compute leak
--decide allow/reject
--SET level
--SET timestamp
--EXPIRE

-- KEYS[1] = bucketKey
-- KEYS[2] = timestampKey

-- ARGV[1] = capacity
-- ARGV[2] = leakRatePerMs
-- ARGV[3] = currentTimeMs
-- ARGV[4] = ttlSeconds

local bucketKey = KEYS[1]
local tsKey = KEYS[2]

local capacity = tonumber(ARGV[1])
local leakRate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local ttl = tonumber(ARGV[4])

local currentLevel = tonumber(redis.call("GET", bucketKey)) or 0
local lastTs = tonumber(redis.call("GET", tsKey)) or now

local elapsed = now - lastTs
local leaked = elapsed * leakRate

currentLevel = math.max(0, currentLevel - leaked)

local allowed = 0
local retryAfterMs = 0

if (currentLevel + 1) <= capacity then
	currentLevel = currentLevel + 1
	allowed = 1
else
	local excess = (currentLevel + 1) - capacity
	retryAfterMs = math.floor(excess / leakRate)
end

redis.call("SET", bucketKey, currentLevel)
redis.call("SET", tsKey, now)

redis.call("EXPIRE", bucketKey, ttl)
redis.call("EXPIRE", tsKey, ttl)

local remaining = math.max(0, capacity - currentLevel)
local resetTime = now + math.floor(currentLevel / leakRate)

return {allowed, remaining, retryAfterMs, resetTime}