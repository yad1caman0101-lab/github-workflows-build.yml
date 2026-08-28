import os
import json
import time
import subprocess
import urllib.parse
import threading
import requests
from datetime import datetime

# ==========================================
# ⚡ AUTO-DISCOVERY GROQ HYPER-CORE
# ==========================================
GROQ_API_KEY = "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"

ACTIVE_MODEL = None
auto_scroll_active = False

def speak(text):
    """Clean Voice Output via Termux TTS"""
    print(f"\n🤖 JARVIS: {text}\n")
    clean_text = text.replace('"', '').replace("'", "").replace("\n", " ").replace("*", "").replace("#", "")
    os.system(f'termux-tts-speak "{clean_text}"')

def listen():
    """Enhanced Voice Listener"""
    print("🎙️ Listening (Bolna shuru karein)...")
    try:
        process = subprocess.Popen(
            ["termux-speech-to-text"],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True
        )
        stdout, _ = process.communicate(timeout=10)
        recognized = stdout.strip().lower()
        if recognized:
            print(f"👂 JARVIS Heard: '{recognized}'")
        return recognized
    except subprocess.TimeoutExpired:
        process.kill()
        return ""
    except Exception:
        return ""

def init_groq_model():
    """Fetches available active chat models directly from Groq Account"""
    global ACTIVE_MODEL
    url = "https://api.groq.com/openai/v1/models"
    headers = {"Authorization": f"Bearer {GROQ_API_KEY.strip()}"}
    try:
        res = requests.get(url, headers=headers, timeout=6)
        data = res.json()
        if "data" in data and len(data["data"]) > 0:
            # Filter valid LLM text models
            for item in data["data"]:
                m_id = item.get("id", "")
                if "whisper" not in m_id and "guard" not in m_id and "embed" not in m_id:
                    ACTIVE_MODEL = m_id
                    print(f"✅ Loaded Active Groq Model: {ACTIVE_MODEL}")
                    return
        # Fallback default
        ACTIVE_MODEL = "llama-3.3-70b-versatile"
    except Exception:
        ACTIVE_MODEL = "llama-3.3-70b-versatile"

def launch_package_force(pkg_name, app_name, fallback_url=None):
    """Clears screen to Home and launches target app"""
    speak(f"Opening {app_name}, Sir.")
    os.system("input keyevent 3")
    time.sleep(0.3)
    if fallback_url:
        os.system(f'termux-open-url "{fallback_url}"')
    else:
        os.system(f"monkey -p {pkg_name} -c android.intent.category.LAUNCHER 1 > /dev/null 2>&1")

def bring_termux_front():
    """Brings Termux App to Foreground"""
    speak("Switching back to Termux console, Sir.")
    os.system("monkey -p com.termux -c android.intent.category.LAUNCHER 1 > /dev/null 2>&1")

def scroll_worker(delay):
    global auto_scroll_active
    while auto_scroll_active:
        time.sleep(delay)
        if not auto_scroll_active:
            break
        print("⚡ [Auto-Scroll] Swiping...")
        os.system("input swipe 500 1400 500 300 250")

def start_auto_scroll(delay=6):
    global auto_scroll_active
    if not auto_scroll_active:
        auto_scroll_active = True
        speak("Auto scroll started.")
        t = threading.Thread(target=scroll_worker, args=(delay,), daemon=True)
        t.start()

def stop_auto_scroll():
    global auto_scroll_active
    if auto_scroll_active:
        auto_scroll_active = False
        speak("Auto scroll stopped.")

def play_youtube_auto(query):
    speak(f"Playing {query} on YouTube, Sir.")
    try:
        cmd = f'yt-dlp "ytsearch1:{query}" --print id --no-warnings'
        video_id = subprocess.check_output(cmd, shell=True, timeout=10).decode("utf-8").strip()
        if video_id:
            os.system(f'termux-open-url "https://www.youtube.com/watch?v={video_id}"')
        else:
            encoded = urllib.parse.quote(query)
            os.system(f'termux-open-url "https://www.youtube.com/results?search_query={encoded}"')
    except Exception:
        encoded = urllib.parse.quote(query)
        os.system(f'termux-open-url "https://www.youtube.com/results?search_query={encoded}"')

def get_weather():
    try:
        res = requests.get("https://wttr.in/?format=%C+%t", timeout=5)
        if res.status_code == 200:
            return f"The current weather is {res.text.strip()}, Sir."
    except Exception:
        pass
    return "Weather data unavailable right now, Sir."

def ask_groq_ai(prompt):
    """Ultra-Fast AI Brain using Dynamically Selected Model"""
    global ACTIVE_MODEL
    print(f"⚡ JARVIS Neural Engine ({ACTIVE_MODEL}) Thinking...")
    url = "https://api.groq.com/openai/v1/chat/completions"
    headers = {
        "Authorization": f"Bearer {GROQ_API_KEY.strip()}",
        "Content-Type": "application/json"
    }

    system_prompt = (
        "You are JARVIS, an ultra-smart, polite, and witty AI assistant. "
        "User communicates via voice in Hindi, Hinglish, or English. "
        "Answer naturally, directly, accurately, and politely strictly in 1 or 2 crisp sentences suitable for spoken audio. "
        "Never use markdown formatting, bullets, asterisks, or robotic intros."
    )

    payload = {
        "model": ACTIVE_MODEL,
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": prompt}
        ],
        "temperature": 0.6,
        "max_tokens": 150
    }

    try:
        response = requests.post(url, headers=headers, json=payload, timeout=8)
        res_data = response.json()

        if "choices" in res_data and len(res_data["choices"]) > 0:
            return res_data["choices"][0]["message"]["content"].strip()

        if "error" in res_data:
            err_msg = res_data['error'].get('message', 'API Error')
            print(f"⚠️ Groq Error: {err_msg}")

    except Exception as e:
        print(f"⚠️ Connection Error: {e}")

    return "Main samajh gaya Sir, batayein aage kya karna hai."

def handle_command(cmd):
    # 1. SHUTDOWN & PAUSE
    if any(word in cmd for word in ["exit", "quit", "power down"]):
        stop_auto_scroll()
        speak("Powering down systems. Goodbye, Sir.")
        return False

    elif any(word in cmd for word in ["ruko", "wait", "chup", "hold on", "pause"]):
        stop_auto_scroll()
        speak("Paused, Sir.")
        return True

    # 2. RETURN TO TERMUX CONSOLE
    elif any(word in cmd for word in ["termax", "thermax", "termux", "karmak", "tamatar", "tarmak"]):
        bring_termux_front()
        return True

    # 3. REELS / SHORTS AUTO-SCROLL
    elif any(w in cmd for w in ["rail", "rel", "reel", "reels", "shorts", "scroll"]):
        if any(w in cmd for w in ["stop", "ruk", "band", "pause"]):
            stop_auto_scroll()
        else:
            if "instagram" in cmd or "insta" in cmd:
                launch_package_force("com.instagram.android", "Instagram", "https://www.instagram.com")
                time.sleep(2)
            elif "youtube" in cmd:
                launch_package_force("com.google.android.youtube", "YouTube", "https://www.youtube.com")
                time.sleep(2)
            start_auto_scroll(delay=6)

    # 4. YOUTUBE SEARCH & AUTO-PLAY
    elif any(w in cmd for w in ["play", "baja", "chala", "gaana", "gana", "song"]):
        song = cmd
        for filler in ["jarvis", "open karke", "kholo aur", "open youtube and", "youtube open karke", 
                       "youtube par", "on youtube", "in youtube", "play kar", "play karo", "play", 
                       "ka gana", "gana", "gaana", "song", "baja do", "chala do", "chala", "achcha", "bhojpuri"]:
            song = song.replace(filler, "")
        song = song.strip()
        if not song:
            song = "khesari lal yadav hit song"
        play_youtube_auto(song)

    # 5. DIRECT APPS
    elif "whatsapp" in cmd:
        launch_package_force("com.whatsapp", "WhatsApp", "https://wa.me/")
    elif "instagram" in cmd or "insta" in cmd:
        launch_package_force("com.instagram.android", "Instagram", "https://www.instagram.com")
    elif "youtube" in cmd:
        launch_package_force("com.google.android.youtube", "YouTube", "https://www.youtube.com")
    elif "chrome" in cmd:
        launch_package_force("com.android.chrome", "Chrome", "https://www.google.com")

    # 6. SYSTEM STATS
    elif any(w in cmd for w in ["weather", "mausam", "mosam", "tapman"]):
        speak(get_weather())
    elif any(w in cmd for w in ["time", "samay", "waqt"]):
        now = datetime.now().strftime("%I:%M %p")
        speak(f"The time is {now}, Sir.")

    # 7. GENERAL AI
    else:
        clean_prompt = cmd.replace("jarvis", "").strip()
        if len(clean_prompt) > 2:
            ans = ask_groq_ai(clean_prompt)
            speak(ans)

    return True

def main():
    os.system("clear")
    print("========================================")
    print("      JARVIS LIVE : GROQ AUTO-CORE      ")
    print("========================================")
    init_groq_model()
    speak("JARVIS Hyper-Core is online and lightning fast.")

    while True:
        query = listen()
        if query:
            if not handle_command(query):
                break
            time.sleep(0.8)
        else:
            time.sleep(1.2)

if __name__ == "__main__":
    main()
