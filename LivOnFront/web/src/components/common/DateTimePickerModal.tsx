import React, { useState, useEffect } from "react";
import styled from "styled-components";
import { BaseModalProps, Overlay } from "./ModalStyles";

const DateTimeModalCard = styled.div`
  width: 520px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
  background: #ffffff;
  border-radius: 16px;
  padding: 24px 20px 20px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
`;

const DateTimeTitle = styled.h2`
  margin: 0 0 16px 0;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 700;
  font-size: 20px;
  color: #111827;
  text-align: center;
`;

const MonthNavigation = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
`;

const MonthButton = styled.button`
  border: none;
  background: transparent;
  font-size: 18px;
  cursor: pointer;
  color: #374151;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  padding: 4px 8px;

  &:hover {
    color: #111827;
  }
`;

const MonthText = styled.span`
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-weight: 700;
  font-size: 18px;
  color: #111827;
`;

const WeekDays = styled.div`
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
  margin-bottom: 10px;
`;

const WeekDay = styled.div<{ $isSunday?: boolean; $isSaturday?: boolean }>`
  text-align: center;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 14px;
  font-weight: 500;
  color: ${(props) => {
    if (props.$isSunday) return "#ef4444";
    if (props.$isSaturday) return "#3b82f6";
    return "#6b7280";
  }};
`;

const CalendarGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
  margin-bottom: 16px;
`;

const CalendarDay = styled.button<{
  $isOtherMonth?: boolean;
  $isSelected?: boolean;
  $isPast?: boolean;
}>`
  aspect-ratio: 1;
  border: ${(props) =>
    props.$isSelected ? "2px solid #5b77f6" : "1px solid transparent"};
  border-radius: ${(props) => (props.$isSelected ? "50%" : "8px")};
  background: ${(props) => (props.$isSelected ? "#5b77f6" : "transparent")};
  color: ${(props) => {
    if (props.$isSelected) return "#ffffff";
    if (props.$isOtherMonth) return "#d1d5db";
    if (props.$isPast) return "#d1d5db";
    return "#111827";
  }};
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 14px;
  font-weight: ${(props) => (props.$isSelected ? "600" : "400")};
  cursor: ${(props) => (props.$isPast ? "not-allowed" : "pointer")};
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: ${(props) => (props.$isPast ? 0.5 : 1)};

  &:hover {
    background: ${(props) => {
      if (props.$isPast) return "transparent";
      return props.$isSelected ? "#4965f6" : "#f3f4f6";
    }};
  }

  &:disabled {
    cursor: not-allowed;
  }
`;

const TimeSection = styled.div`
  margin-bottom: 16px;
`;

const TimeLabel = styled.div`
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 8px;
`;

const TimeButtons = styled.div`
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
`;

const TimeButton = styled.button<{
  $isSelected?: boolean;
  $isBlocked?: boolean;
}>`
  padding: 8px 16px;
  border: ${(props) => (props.$isSelected ? "none" : "1px solid #e5e7eb")};
  border-radius: 8px;
  background: ${(props) => {
    if (props.$isSelected) return "#5b77f6";
    if (props.$isBlocked) return "#f3f4f6";
    return "#ffffff";
  }};
  color: ${(props) => {
    if (props.$isSelected) return "#ffffff";
    if (props.$isBlocked) return "#9ca3af";
    return "#374151";
  }};
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;
  font-size: 13px;
  font-weight: ${(props) => (props.$isSelected ? "600" : "400")};
  cursor: pointer;
  opacity: ${(props) => (props.$isBlocked ? 0.6 : 1)};

  &:hover {
    background: ${(props) => {
      if (props.$isBlocked) return "#e5e7eb";
      return props.$isSelected ? "#4965f6" : "#f3f4f6";
    }};
  }

  &:disabled {
    cursor: not-allowed;
  }
`;

const SelectButton = styled.button`
  width: 100%;
  height: 48px;
  background-color: #5b77f6;
  color: #ffffff;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
    Roboto, sans-serif;

  &:hover {
    background-color: #4965f6;
  }

  &:disabled {
    background-color: #d1d5db;
    cursor: not-allowed;
  }
`;

export interface DateTimePickerModalProps extends BaseModalProps {
  onSelect: (dates: Date[], times: string[]) => void;
  initialDates?: Date[];
  initialTimes?: string[];
  blockedTimesByDate?: Map<string, string[]>; // 날짜별 막힌 시간 (날짜: "YYYY-MM-DD", 시간: "HH:mm" 형식)
  onDateChange?: (dates: Date[]) => void; // 날짜 변경 시 콜백
}

export const DateTimePickerModal: React.FC<DateTimePickerModalProps> = ({
  open,
  onClose,
  onSelect,
  initialDates = [],
  initialTimes = [],
  blockedTimesByDate = new Map(),
  onDateChange,
  className,
  style,
}) => {
  const [currentMonth, setCurrentMonth] = useState(() => {
    const date = initialDates[0] || new Date();
    return new Date(date.getFullYear(), date.getMonth(), 1);
  });

  const [selectedDates, setSelectedDates] = useState<Date[]>(initialDates);
  const [selectedTimes, setSelectedTimes] = useState<string[]>(initialTimes);

  // 모달이 열릴 때 초기 날짜들의 막힌 시간 조회
  useEffect(() => {
    if (open && initialDates.length > 0 && onDateChange) {
      onDateChange(initialDates);
    }
  }, [open, initialDates, onDateChange]);

  const weekDays = ["일", "월", "화", "수", "목", "금", "토"];
  const amTimes = ["8:00", "9:00", "10:00", "11:00"];
  const pmTimes = [
    "12:00",
    "1:00",
    "2:00",
    "3:00",
    "4:00",
    "5:00",
    "6:00",
    "7:00",
    "8:00",
    "9:00",
    "10:00",
  ];

  const getDaysInMonth = (date: Date) => {
    const year = date.getFullYear();
    const month = date.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const daysInMonth = lastDay.getDate();
    const startingDayOfWeek = firstDay.getDay();

    const days: { day: number; isOtherMonth: boolean }[] = [];

    // 이전 달의 마지막 날들
    const prevMonthLastDay = new Date(year, month, 0).getDate();
    for (let i = startingDayOfWeek - 1; i >= 0; i--) {
      days.push({ day: prevMonthLastDay - i, isOtherMonth: true });
    }

    // 현재 달의 날들
    for (let i = 1; i <= daysInMonth; i++) {
      days.push({ day: i, isOtherMonth: false });
    }

    // 다음 달의 첫 날들 (6주를 채우기 위해)
    const remainingDays = 42 - days.length;
    for (let i = 1; i <= remainingDays; i++) {
      days.push({ day: i, isOtherMonth: true });
    }

    return days;
  };

  const handlePrevMonth = () => {
    setCurrentMonth(
      new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1, 1)
    );
  };

  const handleNextMonth = () => {
    setCurrentMonth(
      new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1, 1)
    );
  };

  // 오늘 날짜 확인 함수
  const isPastDate = (date: Date): boolean => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const compareDate = new Date(date);
    compareDate.setHours(0, 0, 0, 0);
    return compareDate < today;
  };

  const handleDayClick = (day: number, isOtherMonth: boolean) => {
    if (isOtherMonth) return;

    const date = new Date(
      currentMonth.getFullYear(),
      currentMonth.getMonth(),
      day
    );

    // 과거 날짜는 선택할 수 없음
    if (isPastDate(date)) return;

    setSelectedDates((prev) => {
      const exists = prev.some(
        (d) =>
          d.getFullYear() === date.getFullYear() &&
          d.getMonth() === date.getMonth() &&
          d.getDate() === date.getDate()
      );

      // 단일 날짜 선택 모드: 같은 날짜를 다시 클릭하면 해제, 아니면 해당 날짜만 선택
      const newDates: Date[] = exists ? [] : [date];

      // 날짜 변경 시 콜백 호출
      if (onDateChange) {
        onDateChange(newDates);
      }

      return newDates;
    });
  };

  // 시간을 "HH:mm" 형식으로 변환
  const convertTimeToHHmm = (time: string, period: "AM" | "PM"): string => {
    const [hour, minute] = time.split(":");
    let hour24 = parseInt(hour, 10);

    if (period === "PM" && hour24 !== 12) {
      hour24 += 12;
    } else if (period === "AM" && hour24 === 12) {
      hour24 = 0;
    }

    return `${String(hour24).padStart(2, "0")}:${minute}`;
  };

  // 선택된 날짜들 중 하나라도 해당 시간이 막혀있는지 확인
  const isTimeBlocked = (time: string, period: "AM" | "PM"): boolean => {
    if (selectedDates.length === 0) return false;

    const timeHHmm = convertTimeToHHmm(time, period);

    // 선택된 날짜들 중 하나라도 해당 시간이 막혀있으면 true
    return selectedDates.some((date) => {
      const dateStr = `${date.getFullYear()}-${String(
        date.getMonth() + 1
      ).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
      const blockedTimes = (blockedTimesByDate.get(dateStr) || []) as string[];

      // blockedTimes 항목은 "HH:mm" 또는 "HH:mm-HH:mm" 범위일 수 있음
      return blockedTimes.some((blocked: string) => {
        if (blocked.includes("-")) {
          const [start, end] = blocked.split("-").map((s: string) => s.trim());
          // timeHHmm가 [start, end) 범위에 포함되면 막힌 것으로 간주
          return timeHHmm >= start && timeHHmm < end;
        }
        // 단일 시간과 정확히 일치하는 경우
        return blocked === timeHHmm;
      });
    });
  };

  const handleTimeClick = (time: string, period: "AM" | "PM") => {
    // 막힌 시간도 클릭 가능하도록 허용 (회색 표시만 유지)
    const timeKey = `${period} ${time}`;
    setSelectedTimes((prev) => {
      if (prev.includes(timeKey)) {
        return prev.filter((t) => t !== timeKey);
      } else {
        return [...prev, timeKey];
      }
    });
  };

  const handleSelect = () => {
    if (selectedDates.length > 0) {
      onSelect(selectedDates, selectedTimes);
      onClose();
    }
  };

  const days = getDaysInMonth(currentMonth);
  const monthYear = `${currentMonth.getMonth() + 1}월`;

  const isSelected = (day: number, isOtherMonth: boolean) => {
    if (isOtherMonth) return false;
    return selectedDates.some(
      (date) =>
        date.getDate() === day &&
        date.getMonth() === currentMonth.getMonth() &&
        date.getFullYear() === currentMonth.getFullYear()
    );
  };

  const isPast = (day: number, isOtherMonth: boolean) => {
    if (isOtherMonth) return false;
    const date = new Date(
      currentMonth.getFullYear(),
      currentMonth.getMonth(),
      day
    );
    return isPastDate(date);
  };

  const hasSelectedDate = selectedDates.length > 0;

  if (!open) return null;

  return (
    <Overlay onClick={onClose}>
      <DateTimeModalCard
        className={className}
        style={style}
        onClick={(e) => e.stopPropagation()}
      >
        <DateTimeTitle>날짜 / 시간 선택</DateTimeTitle>

        <MonthNavigation>
          <MonthButton onClick={handlePrevMonth}>&lt;</MonthButton>
          <MonthText>{monthYear}</MonthText>
          <MonthButton onClick={handleNextMonth}>&gt;</MonthButton>
        </MonthNavigation>

        <WeekDays>
          {weekDays.map((day, index) => (
            <WeekDay
              key={index}
              $isSunday={index === 0}
              $isSaturday={index === 6}
            >
              {day}
            </WeekDay>
          ))}
        </WeekDays>

        <CalendarGrid>
          {days.map(({ day, isOtherMonth }, index) => {
            const selected = isSelected(day, isOtherMonth);
            const past = isPast(day, isOtherMonth);
            return (
              <CalendarDay
                key={index}
                $isOtherMonth={isOtherMonth}
                $isSelected={selected}
                $isPast={past}
                onClick={() => handleDayClick(day, isOtherMonth)}
                disabled={past}
              >
                {day}
              </CalendarDay>
            );
          })}
        </CalendarGrid>

        {hasSelectedDate && (
          <TimeSection>
            <TimeLabel>오전</TimeLabel>
            <TimeButtons>
              {amTimes.map((time) => {
                const timeKey = `AM ${time}`;
                const blocked = isTimeBlocked(time, "AM");
                return (
                  <TimeButton
                    key={time}
                    $isSelected={selectedTimes.includes(timeKey)}
                    $isBlocked={blocked}
                    onClick={() => handleTimeClick(time, "AM")}
                    title={blocked ? "이미 막힌 시간입니다" : ""}
                  >
                    {time}
                  </TimeButton>
                );
              })}
            </TimeButtons>
            <TimeLabel style={{ marginTop: "12px" }}>오후</TimeLabel>
            <TimeButtons>
              {pmTimes.map((time) => {
                const timeKey = `PM ${time}`;
                const blocked = isTimeBlocked(time, "PM");
                return (
                  <TimeButton
                    key={time}
                    $isSelected={selectedTimes.includes(timeKey)}
                    $isBlocked={blocked}
                    onClick={() => handleTimeClick(time, "PM")}
                    title={blocked ? "이미 막힌 시간입니다" : ""}
                  >
                    {time}
                  </TimeButton>
                );
              })}
            </TimeButtons>
          </TimeSection>
        )}

        <SelectButton onClick={handleSelect} disabled={!hasSelectedDate}>
          선택
        </SelectButton>
      </DateTimeModalCard>
    </Overlay>
  );
};

export default DateTimePickerModal;

