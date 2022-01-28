// This is the source code used to compile User30.dll and User34.dll
//
// This DLL is necessary because the ProjectZomboid executables do not
// put the JVM's DLLs on the path, so this code does it for it.

#define WIN32_LEAN_AND_MEAN
#include <Windows.h>

__stdcall int (*mba)(HWND, LPCSTR, LPCSTR, UINT);

__declspec(dllexport) __stdcall extern "C" int MessageBoxA(
        HWND hWnd,
        LPCSTR lpText,
        LPCSTR lpCaption,
        UINT uType) {
    return mba(hWnd, lpText, lpCaption, uType);
}

constexpr auto jre =
#ifdef _WIN64
    L"\\jre64\\bin";
#else
    L"\\jre\\bin";
#endif

void InitializeStub() {
    mba = reinterpret_cast<int (__stdcall*)(HWND, LPCSTR, LPCSTR, UINT)>(GetProcAddress(LoadLibraryA("User32.dll"), "MessageBoxA"));
    WCHAR path[MAX_PATH];
    if (!GetCurrentDirectoryW(MAX_PATH, path))
        return;
    wcscat_s(path, jre);
    SetDllDirectoryW(path);
}

BOOL WINAPI DllMain(HINSTANCE hInst, DWORD reason, LPVOID) {
    switch (reason) {
        case DLL_PROCESS_ATTACH:
            DisableThreadLibraryCalls(hInst);
            InitializeStub();
            break;
    }
    return TRUE;
}
