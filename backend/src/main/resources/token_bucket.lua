local key = KEYS[1]
local max_tokens = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local requested = tonumber(ARGV[4])

-- Load current bucket state
local bucket = redis.call("HMGET", key, "tokens", "timestamp")
local tokens = tonumber(bucket[1]) or max_tokens
local timestamp = tonumber(bucket[2]) or now

-- Calculate how many tokens to add based on time elapsed
local delta = math.max(0, now - timestamp)
tokens = math.min(max_tokens, tokens + delta * refill_rate)

-- Check if request is allowed and decrement
local allowed = tokens >= requested
if allowed then
	tokens = tokens - requested
end

-- Save the new state and set a 60-second expiry to clean up old buckets
redis.call("HMSET", key, "tokens", tokens, "timestamp", now)
redis.call("EXPIRE", key, 60)

if allowed then
	return 1
else
	return 0
end