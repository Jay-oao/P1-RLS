local tokens_key = KEYS[1]
local timestamp_key = KEYS[2]

local capacity = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

-- Get current state
local tokens = tonumber(redis.call("GET", tokens_key))
local last_refill = tonumber(redis.call("GET", timestamp_key))

-- Initialize if not present
if tokens == nil then
	tokens = capacity
end

if last_refill == nil then
	last_refill = now
end

-- Refill tokens
local delta = math.max(0, now - last_refill)
local refill = (delta / 1000.0) * refill_rate
tokens = math.min(capacity, tokens + refill)

-- Check if request allowed
local allowed = 0
if tokens >= 1 then
	tokens = tokens - 1
	allowed = 1
end

-- Save updated state
redis.call("SET", tokens_key, tokens)
redis.call("SET", timestamp_key, now)

-- Set TTL (2x time to fill bucket)
local ttl = math.ceil((capacity / refill_rate) * 2)
redis.call("EXPIRE", tokens_key, ttl)
redis.call("EXPIRE", timestamp_key, ttl)

-- Calculate retryAfter (ms)
local retry_after = 0
if allowed == 0 then
	local tokens_needed = 1 - tokens
	retry_after = math.ceil((tokens_needed / refill_rate) * 1000)
end

-- Calculate reset time (ms to full bucket)
local time_to_full = math.ceil(((capacity - tokens) / refill_rate) * 1000)

-- Return values:
-- [allowed, remaining tokens, retryAfterMs, timeToFullMs]
return {allowed, tokens, retry_after, time_to_full}