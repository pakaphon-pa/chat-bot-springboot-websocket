"use client";
import { useEffect, useRef, useState } from "react";
import { Client, IMessage } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import dynamic from "next/dynamic";
import type { EmojiClickData } from "emoji-picker-react";
import { v4 as uuidv4 } from "uuid";

// ❗ ใช้ dynamic import เพื่อปิด SSR สำหรับตัว EmojiPicker
const EmojiPicker = dynamic(() => import("emoji-picker-react"), { ssr: false });

interface ChatMessage {
  userId: string;
  sender: string;
  content: string;
  timezone: string;
  type: "JOIN" | "CHAT" | "LEAVE";
}

const mockTimeZone = "Asia/Bangkok";
const userId = uuidv4(); // 👈 UUID แทน user

export default function ChatPage() {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState("");
  const [client, setClient] = useState<Client | null>(null);
  const [connected, setConnected] = useState(false);
  const [showEmoji, setShowEmoji] = useState(false);

  // auto-scroll
  const bottomRef = useRef<HTMLDivElement | null>(null);
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, showEmoji]);

  const connect = () => {
    const socket = new SockJS(`http://localhost:8080/ws?userId=${userId}`);
    const c = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      debug: (s) => console.log("STOMP:", s),
    });

    c.onConnect = () => {
      setConnected(true);
      c.subscribe("/user/queue/messages", (msg: IMessage) => {
        const body: ChatMessage = JSON.parse(msg.body);
        console.log("🤖 Bot reply:", msg.body);
        setMessages((prev) => [...prev, body]);
      });
      // ส่ง JOIN
      c.publish({
        destination: "/app/chat",
        body: JSON.stringify({
          userId: userId,
          sender: "User1",
          content: "",
          type: "JOIN",
          timezone: mockTimeZone,
        }),
      });
    };

    c.activate();
    setClient(c);
  };

  const sendMessage = () => {
    if (!client || !input.trim()) return;
    const chat: ChatMessage = {
      userId: userId,
      sender: "User1",
      content: input,
      type: "CHAT",
      timezone: mockTimeZone,
    };
    client.publish({ destination: "/app/chat", body: JSON.stringify(chat) });
    setMessages((prev) => [...prev, chat]);
    setInput("");
    setShowEmoji(false);
  };

  const onEmojiClick = (e: EmojiClickData) => {
    setInput((prev) => prev + e.emoji);
  };

  return (
    <div className="flex items-center justify-center h-screen bg-gray-100">
      {!connected ? (
        <button
          onClick={connect}
          className="px-6 py-3 bg-blue-500 text-white rounded-lg shadow-md"
        >
          เริ่มแชท
        </button>
      ) : (
        <div className="flex flex-col h-[600px] w-[360px] border rounded-lg bg-white">
          {/* messages */}
          <div className="flex-1 overflow-y-auto p-4 space-y-2">
            {messages.map((m, i) => (
              <div
                key={i}
                className={`flex ${
                  m.sender === "User1" ? "justify-end" : "justify-start"
                }`}
              >
                <div
                  className={`px-4 py-2 rounded-lg max-w-xs ${
                    m.sender === "User1"
                      ? "bg-blue-600 text-white"
                      : "bg-gray-200 text-gray-900"
                  }`}
                >
                  {m.content}
                </div>
              </div>
            ))}
            <div ref={bottomRef} />
          </div>

          <div className="relative flex items-center gap-2 border-t p-3 bg-gray-50">
            <button
              onClick={() => setShowEmoji((v) => !v)}
              className="px-3 py-2 text-2xl leading-none"
              title="Emoji"
            >
              😊
            </button>

            {showEmoji && (
              <div className="absolute bottom-16 left-3 z-10">
                <EmojiPicker
                  onEmojiClick={onEmojiClick}
                  autoFocusSearch
                  searchDisabled={false}
                  lazyLoadEmojis
                  width={320}
                  height={420}
                  previewConfig={{ showPreview: false }}
                />
              </div>
            )}

            <input
              className="flex-1 px-4 py-2 rounded-full border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-400 text-black"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="พิมพ์ข้อความ..."
              onKeyDown={(e) => e.key === "Enter" && sendMessage()}
            />

            <button
              onClick={sendMessage}
              className="px-5 py-2 bg-blue-500 hover:bg-blue-600 text-white font-medium rounded-full shadow"
            >
              ส่ง
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
