import { useState } from "react";
import { Bell, CheckCheck } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

type NotificationItem = { id: string; title: string; body: string; time: string; read: boolean };

const initialItems: NotificationItem[] = [
  {
    id: "1",
    title: "Test run finished",
    body: "Your latest API test batch completed with 2 failures.",
    time: "5 min ago",
    read: false,
  },
  {
    id: "2",
    title: "New insight available",
    body: "AI generated a root-cause summary for POST /api/v1/orders.",
    time: "1 hr ago",
    read: false,
  },
  {
    id: "3",
    title: "Weekly summary",
    body: "You ran 48 tests across 6 endpoints this week.",
    time: "Yesterday",
    read: true,
  },
];

export default function Notifications() {
  const [items, setItems] = useState(initialItems);
  const unreadCount = items.filter((i) => !i.read).length;

  const markAllRead = () => {
    setItems((prev) => prev.map((i) => ({ ...i, read: true })));
  };

  const toggleRead = (id: string) => {
    setItems((prev) => prev.map((i) => (i.id === id ? { ...i, read: !i.read } : i)));
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="space-y-1">
          <h1 className="flex items-center gap-2 text-2xl font-bold tracking-tight">
            <Bell className="h-6 w-6 text-primary" />
            Notifications
          </h1>
          <p className="text-sm text-muted-foreground">
            {unreadCount > 0 ? `${unreadCount} unread` : "You’re all caught up."}
          </p>
        </div>
        {unreadCount > 0 ? (
          <Button type="button" variant="outline" size="sm" onClick={markAllRead}>
            <CheckCheck className="mr-2 h-4 w-4" />
            Mark all read
          </Button>
        ) : null}
      </div>

      <Card className="shadow-card">
        <CardHeader>
          <CardTitle className="text-lg">Inbox</CardTitle>
          <CardDescription>Click a row to toggle read state.</CardDescription>
        </CardHeader>
        <CardContent className="p-0">
          <ul className="divide-y">
            {items.map((item) => (
              <li key={item.id}>
                <button
                  type="button"
                  onClick={() => toggleRead(item.id)}
                  className={cn(
                    "flex w-full flex-col gap-1 px-6 py-4 text-left transition-colors hover:bg-muted/50",
                    !item.read && "bg-primary/5",
                  )}
                >
                  <div className="flex items-start justify-between gap-3">
                    <span className={cn("text-sm font-medium", !item.read && "text-foreground")}>{item.title}</span>
                    <span className="shrink-0 text-xs text-muted-foreground">{item.time}</span>
                  </div>
                  <p className="text-sm text-muted-foreground">{item.body}</p>
                </button>
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>
    </div>
  );
}
